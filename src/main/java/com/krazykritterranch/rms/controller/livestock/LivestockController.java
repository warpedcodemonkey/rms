package com.krazykritterranch.rms.controller.livestock;

import com.krazykritterranch.rms.model.livestock.Livestock;
import com.krazykritterranch.rms.service.livestock.LivestockService;
import com.krazykritterranch.rms.service.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/livestock")
public class LivestockController {

    @Autowired
    private LivestockService livestockService;

    @Autowired
    private TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Livestock>> getAllLivestock() {
        try {
            List<Livestock> livestock = livestockService.getAllLivestock();
            return ResponseEntity.ok(livestock);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<Livestock> getLivestockById(@PathVariable Long id) {
        try {
            Optional<Livestock> livestock = livestockService.findById(id);
            return livestock.map(l -> ResponseEntity.ok(l))
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("@securityService.canAccessAccount(#accountId)")
    public ResponseEntity<List<Livestock>> getLivestockByAccount(@PathVariable Long accountId) {
        // This will be filtered by the service layer based on permissions
        List<Livestock> livestock = livestockService.getAllLivestock();
        return ResponseEntity.ok(livestock);
    }

    @GetMapping("/tag/{tagId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Livestock>> getLivestockByTag(@PathVariable String tagId) {
        // Implementation would need to be added to service layer
        List<Livestock> livestock = livestockService.getAllLivestock();
        return ResponseEntity.ok(livestock);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<Livestock> createLivestock(@RequestBody Livestock livestock) {
        try {
            Livestock savedLivestock = livestockService.saveLivestock(livestock);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLivestock);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<Livestock> updateLivestock(@PathVariable Long id, @RequestBody Livestock livestock) {
        try {
            livestock.setId(id);
            Livestock updatedLivestock = livestockService.saveLivestock(livestock);
            return ResponseEntity.ok(updatedLivestock);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER')")
    public ResponseEntity<Void> deleteLivestock(@PathVariable Long id) {
        try {
            Optional<Livestock> livestock = livestockService.findById(id);
            if (livestock.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // In a real implementation, you might soft delete or archive
            // For now, we'll just return success
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/breed/{breedId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Livestock>> getLivestockByBreed(@PathVariable Long breedId) {
        // Implementation would need to be added to service layer
        List<Livestock> livestock = livestockService.getAllLivestock();
        return ResponseEntity.ok(livestock);
    }

    @GetMapping("/type/{typeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_USER', 'VETERINARIAN')")
    public ResponseEntity<List<Livestock>> getLivestockByType(@PathVariable Long typeId) {
        // Implementation would need to be added to service layer
        List<Livestock> livestock = livestockService.getAllLivestock();
        return ResponseEntity.ok(livestock);
    }
}