package com.krazykritterranch.rms.service.livestock;

import com.krazykritterranch.rms.model.livestock.Feed;
import com.krazykritterranch.rms.model.user.VetPermissionType;
import com.krazykritterranch.rms.repositories.livestock.FeedRepository;
import com.krazykritterranch.rms.repositories.common.AccountRepository;
import com.krazykritterranch.rms.service.security.SecurityService;
import com.krazykritterranch.rms.service.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FeedService {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private TenantContext tenantContext;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Get all feed records accessible to the current user
     */
    public List<Feed> getAllFeed() {
        if (tenantContext.isAdmin()) {
            return feedRepository.findAll();
        } else if (tenantContext.isAccountUser()) {
            return feedRepository.findByAccountId(tenantContext.getCurrentAccountId());
        } else if (tenantContext.isVeterinarian()) {
            return feedRepository.findByVeterinarianAccess(tenantContext.getCurrentUserId());
        }
        throw new SecurityException("Access denied");
    }

    /**
     * Find feed by ID with security checks
     */
    public Optional<Feed> findById(Long id) {
        Optional<Feed> feed = feedRepository.findById(id);
        if (feed.isEmpty()) {
            return Optional.empty();
        }

        // Security check
        if (!canAccessFeed(feed.get())) {
            throw new SecurityException("Access denied");
        }

        return feed;
    }

    /**
     * Find feed by type within current tenant
     */
    public List<Feed> findByFeedType(String feedType) {
        if (tenantContext.isAdmin()) {
            return feedRepository.findAll().stream()
                    .filter(f -> feedType.equals(f.getFeedType()))
                    .toList();
        } else if (tenantContext.isAccountUser()) {
            return feedRepository.findByFeedTypeAndAccount(feedType, tenantContext.getCurrentAccountId());
        } else if (tenantContext.isVeterinarian()) {
            return feedRepository.findByVeterinarianAccess(tenantContext.getCurrentUserId()).stream()
                    .filter(f -> feedType.equals(f.getFeedType()))
                    .toList();
        }
        throw new SecurityException("Access denied");
    }

    /**
     * Save feed with security checks
     */
    public Feed saveFeed(Feed feed) {
        // Security check for editing existing feed
        if (feed.getId() != null && !canEditFeed(feed)) {
            throw new SecurityException("Edit access denied");
        }

        // Set account for new feed
        if (feed.getId() == null && tenantContext.isAccountUser()) {
            feed.setAccount(accountRepository.findById(tenantContext.getCurrentAccountId()).orElse(null));
        }

        // Validate account is set
        if (feed.getAccount() == null) {
            throw new IllegalArgumentException("Feed must be associated with an account");
        }

        return feedRepository.save(feed);
    }

    /**
     * Delete feed with security checks
     */
    public void deleteFeed(Long id) {
        Optional<Feed> feed = findById(id); // This already includes security checks
        if (feed.isEmpty()) {
            throw new IllegalArgumentException("Feed not found");
        }

        if (!canEditFeed(feed.get())) {
            throw new SecurityException("Delete access denied");
        }

        feedRepository.deleteById(id);
    }

    /**
     * Update existing feed with security checks
     */
    public Feed updateFeed(Long id, Feed feedData) {
        Optional<Feed> existingFeed = findById(id); // This already includes security checks
        if (existingFeed.isEmpty()) {
            throw new IllegalArgumentException("Feed not found");
        }

        Feed feed = existingFeed.get();

        if (!canEditFeed(feed)) {
            throw new SecurityException("Edit access denied");
        }

        // Update fields
        feed.setFeedName(feedData.getFeedName());
        feed.setFeedType(feedData.getFeedType());
        feed.setDescription(feedData.getDescription());
        feed.setVendor(feedData.getVendor());

        return feedRepository.save(feed);
    }

    /**
     * Check if current user can access the feed
     */
    private boolean canAccessFeed(Feed feed) {
        if (tenantContext.isAdmin()) {
            return true;
        }

        if (tenantContext.isAccountUser()) {
            return feed.getAccount() != null &&
                    feed.getAccount().getId().equals(tenantContext.getCurrentAccountId());
        }

        if (tenantContext.isVeterinarian()) {
            return feed.getAccount() != null &&
                    securityService.hasVetPermission(feed.getAccount().getId(), VetPermissionType.VIEW_LIVESTOCK);
        }

        return false;
    }

    /**
     * Check if current user can edit the feed
     */
    private boolean canEditFeed(Feed feed) {
        if (tenantContext.isAdmin() || tenantContext.isAccountUser()) {
            return canAccessFeed(feed);
        }

        if (tenantContext.isVeterinarian()) {
            return feed.getAccount() != null &&
                    securityService.hasVetPermission(feed.getAccount().getId(), VetPermissionType.EDIT_LIVESTOCK);
        }

        return false;
    }
}