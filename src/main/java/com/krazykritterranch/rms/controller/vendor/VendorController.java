package com.krazykritterranch.rms.controller.vendor;

import com.krazykritterranch.rms.model.vendor.Vendor;
import com.krazykritterranch.rms.model.vendor.VendorType;
import com.krazykritterranch.rms.service.vendor.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Vendor>> getAllVendors(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        try {
            List<Vendor> vendors = activeOnly ?
                    vendorService.getActiveVendors() :
                    vendorService.getAllVendors();
            return ResponseEntity.ok(vendors);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<Vendor> getById(@PathVariable Long id) {
        try {
            return vendorService.findById(id)
                    .map(vendor -> ResponseEntity.ok(vendor))
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<Vendor> createVendor(@RequestBody Vendor vendor) {
        try {
            Vendor savedVendor = vendorService.createVendor(vendor);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVendor);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<Vendor> updateVendor(@PathVariable Long id, @RequestBody Vendor vendor) {
        try {
            Vendor updatedVendor = vendorService.updateVendor(id, vendor);
            return ResponseEntity.ok(updatedVendor);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        try {
            vendorService.deleteVendor(id);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Vendor>> searchByName(@RequestParam String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            List<Vendor> vendors = vendorService.searchByName(name.trim());
            return ResponseEntity.ok(vendors);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/type/{typeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Vendor>> getVendorsByType(@PathVariable Long typeId) {
        try {
            List<Vendor> vendors = vendorService.getVendorsByType(typeId);
            return ResponseEntity.ok(vendors);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<Map<String, Long>> getVendorCounts() {
        try {
            Map<String, Long> counts = new HashMap<>();
            counts.put("total", vendorService.countVendors());
            counts.put("active", vendorService.countActiveVendors());
            return ResponseEntity.ok(counts);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Vendor Type endpoints

    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<List<VendorType>> getAllVendorTypes() {
        try {
            List<VendorType> types = vendorService.getAllVendorTypes();
            return ResponseEntity.ok(types);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/types")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<VendorType> createVendorType(@RequestBody VendorType vendorType) {
        try {
            VendorType savedType = vendorService.createVendorType(vendorType);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedType);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}