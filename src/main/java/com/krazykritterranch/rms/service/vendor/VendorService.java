package com.krazykritterranch.rms.service.vendor;

import com.krazykritterranch.rms.model.vendor.Vendor;
import com.krazykritterranch.rms.model.vendor.VendorType;
import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.model.user.VetPermissionType;
import com.krazykritterranch.rms.repositories.vendor.VendorRepository;
import com.krazykritterranch.rms.repositories.vendor.VendorTypeRepository;
import com.krazykritterranch.rms.repositories.common.AccountRepository;
import com.krazykritterranch.rms.service.security.TenantContext;
import com.krazykritterranch.rms.service.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VendorTypeRepository vendorTypeRepository;

    @Autowired
    private TenantContext tenantContext;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Get all vendors for the current account
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public List<Vendor> getAllVendors() {
        if (tenantContext.isAdmin()) {
            Long accountId = tenantContext.getCurrentAccountId();
            if (accountId != null) {
                return vendorRepository.findByAccountId(accountId);
            }
            return vendorRepository.findAll();
        } else if (tenantContext.isAccountUser()) {
            Long accountId = tenantContext.getCurrentAccountId();
            if (accountId == null) {
                throw new SecurityException("No account context available");
            }
            return vendorRepository.findByAccountId(accountId);
        } else if (tenantContext.isVeterinarian()) {
            // Veterinarians can see vendors through feed relationships
            Long vetId = tenantContext.getCurrentUserId();
            return vendorRepository.findByVeterinarianAccess(vetId);
        }
        throw new SecurityException("Access denied");
    }

    /**
     * Get active vendors only
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public List<Vendor> getActiveVendors() {
        Long accountId = tenantContext.getCurrentAccountId();
        if (accountId == null && !tenantContext.isAdmin()) {
            throw new SecurityException("No account context available");
        }

        if (tenantContext.isVeterinarian()) {
            // Filter the vet-accessible vendors to only active ones
            return getAllVendors().stream()
                    .filter(Vendor::getIsActive)
                    .toList();
        }

        return vendorRepository.findActiveByAccountId(accountId);
    }

    /**
     * Find vendor by ID
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public Optional<Vendor> findById(Long id) {
        Optional<Vendor> vendor = vendorRepository.findById(id);
        if (vendor.isEmpty()) {
            return Optional.empty();
        }

        // Security check
        if (!canAccessVendor(vendor.get())) {
            throw new SecurityException("Access denied");
        }

        return vendor;
    }

    /**
     * Create a new vendor
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public Vendor createVendor(Vendor vendor) {
        Long accountId = tenantContext.getCurrentAccountId();
        if (accountId == null && !tenantContext.isAdmin()) {
            throw new SecurityException("No account context available");
        }

        // Set the account
        if (tenantContext.isAdmin() && vendor.getAccount() == null) {
            throw new IllegalArgumentException("Account must be specified");
        } else if (tenantContext.isAccountUser()) {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            vendor.setAccount(account);
        }

        // Validate vendor type belongs to the same account
        if (vendor.getVendorType() != null) {
            VendorType vendorType = vendorTypeRepository.findByIdAndAccountId(
                    vendor.getVendorType().getId(),
                    vendor.getAccount().getId()
            ).orElseThrow(() -> new IllegalArgumentException("Invalid vendor type for this account"));
            vendor.setVendorType(vendorType);
        }

        // Set default active status
        if (vendor.getIsActive() == null) {
            vendor.setIsActive(true);
        }

        return vendorRepository.save(vendor);
    }

    /**
     * Update an existing vendor
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public Vendor updateVendor(Long id, Vendor vendorDetails) {
        Vendor existingVendor = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found"));

        if (!canEditVendor(existingVendor)) {
            throw new SecurityException("Not authorized to edit this vendor");
        }

        // Update fields
        existingVendor.setVendorName(vendorDetails.getVendorName());
        existingVendor.setWebsite(vendorDetails.getWebsite());
        existingVendor.setPrimaryContactName(vendorDetails.getPrimaryContactName());
        existingVendor.setIsActive(vendorDetails.getIsActive());

        // Update vendor type if provided and valid
        if (vendorDetails.getVendorType() != null) {
            VendorType vendorType = vendorTypeRepository.findByIdAndAccountId(
                    vendorDetails.getVendorType().getId(),
                    existingVendor.getAccount().getId()
            ).orElseThrow(() -> new IllegalArgumentException("Invalid vendor type for this account"));
            existingVendor.setVendorType(vendorType);
        }

        // Update collections if provided
        if (vendorDetails.getEmails() != null) {
            existingVendor.setEmails(vendorDetails.getEmails());
        }
        if (vendorDetails.getPhones() != null) {
            existingVendor.setPhones(vendorDetails.getPhones());
        }
        if (vendorDetails.getAddresses() != null) {
            existingVendor.setAddresses(vendorDetails.getAddresses());
        }
        if (vendorDetails.getProducts() != null) {
            existingVendor.setProducts(vendorDetails.getProducts());
        }

        return vendorRepository.save(existingVendor);
    }

    /**
     * Delete a vendor (soft delete by deactivating)
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public void deleteVendor(Long id) {
        Vendor vendor = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found"));

        if (!canEditVendor(vendor)) {
            throw new SecurityException("Not authorized to delete this vendor");
        }

        // Soft delete by deactivating
        vendor.setIsActive(false);
        vendorRepository.save(vendor);
    }

    /**
     * Search vendors by name
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public List<Vendor> searchByName(String name) {
        Long accountId = tenantContext.getCurrentAccountId();
        if (accountId == null && !tenantContext.isAdmin()) {
            throw new SecurityException("No account context available");
        }

        if (tenantContext.isVeterinarian()) {
            // Filter vet-accessible vendors by name
            return getAllVendors().stream()
                    .filter(v -> v.getVendorName().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        }

        return vendorRepository.findByNameContainingAndAccountId(name, accountId);
    }

    /**
     * Get vendors by type
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public List<Vendor> getVendorsByType(Long vendorTypeId) {
        Long accountId = tenantContext.getCurrentAccountId();
        if (accountId == null && !tenantContext.isAdmin()) {
            throw new SecurityException("No account context available");
        }

        if (tenantContext.isVeterinarian()) {
            // Filter vet-accessible vendors by type
            return getAllVendors().stream()
                    .filter(v -> v.getVendorType() != null &&
                            v.getVendorType().getId().equals(vendorTypeId))
                    .toList();
        }

        return vendorRepository.findByVendorTypeIdAndAccountId(vendorTypeId, accountId);
    }

    /**
     * Count vendors for current account
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public long countVendors() {
        Long accountId = tenantContext.getCurrentAccountId();
        if (accountId == null) {
            throw new SecurityException("No account context available");
        }

        return vendorRepository.countByAccountId(accountId);
    }

    /**
     * Count active vendors for current account
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public long countActiveVendors() {
        Long accountId = tenantContext.getCurrentAccountId();
        if (accountId == null) {
            throw new SecurityException("No account context available");
        }

        return vendorRepository.countActiveByAccountId(accountId);
    }

    // Vendor Type Management Methods

    /**
     * Get all vendor types for current account
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public List<VendorType> getAllVendorTypes() {
        Long accountId = tenantContext.getCurrentAccountId();
        if (accountId == null) {
            throw new SecurityException("No account context available");
        }

        return vendorTypeRepository.findByAccountId(accountId);
    }

    /**
     * Create a vendor type
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public VendorType createVendorType(VendorType vendorType) {
        Long accountId = tenantContext.getCurrentAccountId();
        if (accountId == null) {
            throw new SecurityException("No account context available");
        }

        // Check for duplicate name
        if (vendorTypeRepository.existsByTypeNameAndAccountId(vendorType.getTypeName(), accountId)) {
            throw new IllegalArgumentException("Vendor type with this name already exists");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        vendorType.setAccount(account);

        // Set display order if not provided
        if (vendorType.getDisplayOrder() == null) {
            Integer maxOrder = vendorTypeRepository.getMaxDisplayOrderByAccountId(accountId);
            vendorType.setDisplayOrder(maxOrder + 1);
        }

        return vendorTypeRepository.save(vendorType);
    }

    /**
     * Initialize default vendor types for a new account
     */
    @Transactional
    public void initializeDefaultVendorTypes(Account account) {
        // Create default vendor types
        String[] defaultTypes = {
                "Feed Supplier",
                "Medical Supplier",
                "Butcher Facility",
                "Equipment Supplier",
                "Veterinary Services",
                "Transportation",
                "Other"
        };

        int order = 0;
        for (String typeName : defaultTypes) {
            VendorType type = new VendorType(typeName, null, account);
            type.setDisplayOrder(order++);
            type.setIsSystemDefault(false); // These are account-specific copies
            vendorTypeRepository.save(type);
        }
    }

    // Helper methods
    private boolean canAccessVendor(Vendor vendor) {
        if (tenantContext.isAdmin()) {
            return true;
        }

        if (tenantContext.isAccountUser()) {
            return vendor.getAccount() != null &&
                    vendor.getAccount().getId().equals(tenantContext.getCurrentAccountId());
        }

        if (tenantContext.isVeterinarian()) {
            // Veterinarians can access vendors through feed relationships
            Long vetId = tenantContext.getCurrentUserId();
            List<Vendor> accessibleVendors = vendorRepository.findByVeterinarianAccess(vetId);
            return accessibleVendors.stream().anyMatch(v -> v.getId().equals(vendor.getId()));
        }

        return false;
    }

    private boolean canEditVendor(Vendor vendor) {
        if (tenantContext.isAdmin()) {
            return true;
        }

        if (tenantContext.isAccountUser()) {
            return canAccessVendor(vendor);
        }

        // Veterinarians cannot edit vendors
        return false;
    }
}