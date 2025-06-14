package com.krazykritterranch.rms.service.system;

import com.fasterxml.jackson.databind.util.AccessPattern;
import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.model.common.AccountStatus;
import com.krazykritterranch.rms.model.user.*;
import com.krazykritterranch.rms.repositories.common.AccountRepository;
import com.krazykritterranch.rms.repositories.user.PermissionRepository;
import com.krazykritterranch.rms.repositories.user.RoleRepository;
import com.krazykritterranch.rms.repositories.user.UserRepository;
import com.krazykritterranch.rms.service.common.AccountService;
import com.krazykritterranch.rms.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;

@Service
@Transactional
public class SystemInitializationService implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Override
    public void run(String... args) throws Exception {
        initializePermissions();
        initializeRoles();
        createTestAccount();      // Create account first
        createTestUser();         // Then create user with account
        createAdminTestUser();
    }





    private void createTestAccount() {
        // Check if test account already exists
        if (accountRepository.findByFarmName("Test Ranch").isEmpty()) {
            Account testAccount = new Account();
            testAccount.setFarmName("Test Ranch");
            testAccount.setStatus(AccountStatus.ACTIVE);
            testAccount.setSignupDate(new java.sql.Date(System.currentTimeMillis()));
            testAccount.setMembershipStart(new java.sql.Date(System.currentTimeMillis()));
            // No end date for now

            accountRepository.save(testAccount);
            System.out.println("Created test account: Test Ranch");
        }
    }

    private void createTestUser() {
        // Check if test user already exists
        if (userRepository.findByUsername("testuser").isEmpty()) {
            // Get the test account
            Account testAccount = accountRepository.findByFarmName("Test Ranch")
                    .orElseThrow(() -> new RuntimeException("Test account not found"));

            // Create a Customer instance
            Customer testUser = new Customer("testuser", "test@example.com", "password123", "John", "Doe");
            testUser.setPassword(passwordEncoder.encode("password123"));
            testUser.setIsActive(true);
            testUser.setPrimaryAccount(testAccount);  // Set the primary account
            testUser.setCustomerNumber("CUST001");
            testUser.setEmergencyContact("Jane Doe");
            testUser.setEmergencyPhone("555-0124");

            // Add customer role
            roleRepository.findByName("CUSTOMER").ifPresent(role -> {
                testUser.getRoles().add(role);
            });

            userRepository.save(testUser);
            System.out.println("Created test user: testuser / password123 with account");
        }
    }

    private void createAdminTestUser() {
        // Check if test admin user already exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            // Create an Administrator instance
            Administrator testAdmin = new Administrator("admin", "admin@example.com", "admin123", "Admin", "User");
            testAdmin.setPassword(passwordEncoder.encode("admin123"));
            testAdmin.setIsActive(true);
            testAdmin.setDepartment("IT");
            testAdmin.setAccessLevel(10);
            // NOTE: Administrators don't have a primaryAccount - they are system users

            // Add admin role
            roleRepository.findByName("ADMINISTRATOR").ifPresent(role -> {
                testAdmin.getRoles().add(role);
            });

            userRepository.save(testAdmin);
            System.out.println("Created test admin: admin / admin123");
        }
    }

    private void initializePermissions() {
        for (SystemPermissions sysPerm : SystemPermissions.values()) {
            Optional<Permission> existing = permissionRepository.findByName(sysPerm.getName());
            if (existing.isEmpty()) {
                Permission permission = new Permission(
                        sysPerm.getName(),
                        sysPerm.getDescription(),
                        sysPerm.getCategory(),
                        true // isSystemPermission
                );
                permissionRepository.save(permission);
            }
        }
    }



    private void initializeRoles() {
        for (SystemRoles sysRole : SystemRoles.values()) {
            Optional<Role> existing = roleRepository.findByName(sysRole.getName());
            if (existing.isEmpty()) {
                Role role = new Role(sysRole.getName(), sysRole.getDescription(), true);

                // Assign appropriate permissions based on role
                assignPermissionsToRole(role, sysRole);

                roleRepository.save(role);
            }
        }
    }

    private void assignPermissionsToRole(Role role, SystemRoles systemRole) {
        switch (systemRole) {
            case CUSTOMER:
                assignPermissions(role, Arrays.asList(
                        SystemPermissions.CUSTOMER_READ_OWN,
                        SystemPermissions.CUSTOMER_UPDATE_OWN,
                        SystemPermissions.CUSTOMER_MANAGE_VET_PERMISSIONS,
                        SystemPermissions.USER_READ_OWN_PROFILE,
                        SystemPermissions.USER_UPDATE_OWN_PROFILE,
                        SystemPermissions.USER_CHANGE_PASSWORD
                ));
                break;
            case VETERINARIAN:
                assignPermissions(role, Arrays.asList(
                        SystemPermissions.VET_READ_AUTHORIZED_CUSTOMERS,
                        SystemPermissions.VET_UPDATE_AUTHORIZED_CUSTOMERS,
                        SystemPermissions.VET_READ_OWN,
                        SystemPermissions.VET_UPDATE_OWN,
                        SystemPermissions.USER_READ_OWN_PROFILE,
                        SystemPermissions.USER_UPDATE_OWN_PROFILE,
                        SystemPermissions.USER_CHANGE_PASSWORD
                ));
                break;
            case ADMINISTRATOR:
                assignPermissions(role, Arrays.asList(
                        SystemPermissions.ADMIN_READ_ALL_USERS,
                        SystemPermissions.ADMIN_UPDATE_ALL_USERS,
                        SystemPermissions.ADMIN_DELETE_USERS,
                        SystemPermissions.ADMIN_MANAGE_ROLES,
                        SystemPermissions.ADMIN_SYSTEM_SETTINGS,
                        SystemPermissions.USER_READ_OWN_PROFILE,
                        SystemPermissions.USER_UPDATE_OWN_PROFILE,
                        SystemPermissions.USER_CHANGE_PASSWORD
                ));
                break;
            case SUPER_ADMIN:
                // Super admin gets all permissions
                role.getPermissions().clear();
                role.getPermissions().addAll(
                        permissionRepository.findByIsSystemPermissionTrue()
                );
                break;
        }
    }

    private void assignPermissions(Role role, java.util.List<SystemPermissions> permissions) {
        for (SystemPermissions sysPerm : permissions) {
            permissionRepository.findByName(sysPerm.getName())
                    .ifPresent(permission -> role.getPermissions().add(permission));
        }
    }
}