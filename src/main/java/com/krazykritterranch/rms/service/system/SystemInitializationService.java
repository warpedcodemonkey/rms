package com.krazykritterranch.rms.service.system;

import com.krazykritterranch.rms.model.user.Permission;
import com.krazykritterranch.rms.model.user.Role;
import com.krazykritterranch.rms.model.user.SystemPermissions;
import com.krazykritterranch.rms.model.user.SystemRoles;
import com.krazykritterranch.rms.repositories.user.PermissionRepository;
import com.krazykritterranch.rms.repositories.user.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
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

    @Override
    public void run(String... args) throws Exception {
        initializePermissions();
        initializeRoles();
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