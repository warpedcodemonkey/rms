package com.krazykritterranch.rms.controller.livestock;

import com.krazykritterranch.rms.model.livestock.Feed;
import com.krazykritterranch.rms.service.livestock.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    @Autowired
    private FeedService feedService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Feed>> getAllFeed() {
        try {
            List<Feed> feeds = feedService.getAllFeed();
            return ResponseEntity.ok(feeds);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<Feed> getFeedById(@PathVariable Long id) {
        try {
            Optional<Feed> feed = feedService.findById(id);
            return feed.map(f -> ResponseEntity.ok(f))
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/type/{feedType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Feed>> getFeedByType(@PathVariable String feedType) {
        try {
            List<Feed> feeds = feedService.findByFeedType(feedType);
            return ResponseEntity.ok(feeds);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<Feed> createFeed(@RequestBody Feed feed) {
        try {
            // Ensure ID is null for new records
            feed.setId(null);
            Feed savedFeed = feedService.saveFeed(feed);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeed);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<Feed> updateFeed(@PathVariable Long id, @RequestBody Feed feed) {
        try {
            Feed updatedFeed = feedService.updateFeed(id, feed);
            return ResponseEntity.ok(updatedFeed);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long id) {
        try {
            feedService.deleteFeed(id);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Additional endpoint for getting feed by vendor (useful for reporting)
    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Feed>> getFeedByVendor(@PathVariable Long vendorId) {
        try {
            // Filter feeds by vendor from the accessible feeds
            List<Feed> feeds = feedService.getAllFeed().stream()
                    .filter(f -> f.getVendor() != null && f.getVendor().getId().equals(vendorId))
                    .toList();
            return ResponseEntity.ok(feeds);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}