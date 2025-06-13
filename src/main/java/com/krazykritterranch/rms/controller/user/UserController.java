package com.krazykritterranch.rms.controller.user;

import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.service.user.UserService;
import com.krazykritterranch.rms.service.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TenantContext tenantContext;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        // Security is handled in the service layer
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("@securityService.canAccessAccount(#accountId)")
    public ResponseEntity<List<User>> getUsersByAccount(@PathVariable Long accountId) {
        List<User> users = userService.getUsersByAccount(accountId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/administrators")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllAdministrators() {
        List<User> admins = userService.getAllAdministrators();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/veterinarians")
    public ResponseEntity<List<User>> getAllVeterinarians() {
        List<User> vets = userService.getAllVeterinarians();
        return ResponseEntity.ok(vets);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User savedUser = userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            User updatedUser = userService.saveUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateUser(@PathVariable Long id) {
        try {
            userService.reactivateUser(id);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Role management
    @PostMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignRole(@PathVariable Long userId, @PathVariable String roleName) {
        userService.assignRole(userId, roleName);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeRole(@PathVariable Long userId, @PathVariable String roleName) {
        userService.removeRole(userId, roleName);
        return ResponseEntity.ok().build();
    }

    // Check if account can add more users
    @GetMapping("/account/{accountId}/can-add-user")
    @PreAuthorize("@securityService.canAccessAccount(#accountId)")
    public ResponseEntity<Boolean> canAddUserToAccount(@PathVariable Long accountId) {
        boolean canAdd = userService.canAddUserToAccount(accountId);
        return ResponseEntity.ok(canAdd);
    }
}