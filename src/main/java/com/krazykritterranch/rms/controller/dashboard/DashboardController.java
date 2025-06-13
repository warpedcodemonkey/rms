package com.krazykritterranch.rms.controller.dashboard;

import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.model.livestock.Livestock;
import com.krazykritterranch.rms.service.common.AccountService;
import com.krazykritterranch.rms.service.livestock.LivestockService;
import com.krazykritterranch.rms.service.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private LivestockService livestockService;

    @Autowired
    private TenantContext tenantContext;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Account> allAccounts = accountService.findAllForAdmin();
        dashboard.put("totalAccounts", allAccounts.size());
        dashboard.put("activeAccounts", allAccounts.stream()
                .mapToLong(a -> a.getStatus().name().equals("ACTIVE") ? 1 : 0).sum());

        // Add more admin statistics as needed

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/account")
    @PreAuthorize("hasRole('ACCOUNT_USER')")
    public ResponseEntity<Map<String, Object>> getAccountDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        Long accountId = tenantContext.getCurrentAccountId();
        if (accountId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<Livestock> livestock = livestockService.getAllLivestock();
        dashboard.put("totalLivestock", livestock.size());

        // Add livestock by type breakdown
        Map<String, Long> livestockByType = new HashMap<>();
        livestock.forEach(l -> {
            String type = l.getLivestockType() != null ? l.getLivestockType().getLivestockType() : "Unknown";
            livestockByType.merge(type, 1L, Long::sum);
        });
        dashboard.put("livestockByType", livestockByType);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/vet")
    @PreAuthorize("hasRole('VETERINARIAN')")
    public ResponseEntity<Map<String, Object>> getVetDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Account> accessibleAccounts = accountService.getAccountsForVet(tenantContext.getCurrentUserId());
        dashboard.put("accessibleAccounts", accessibleAccounts.size());

        List<Livestock> accessibleLivestock = livestockService.getAllLivestock();
        dashboard.put("accessibleLivestock", accessibleLivestock.size());

        return ResponseEntity.ok(dashboard);
    }
}