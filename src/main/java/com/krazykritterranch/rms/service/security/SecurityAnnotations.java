package com.krazykritterranch.rms.service.security;

import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class SecurityAnnotations {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityService.canAccessAccount(#accountId)")
    public @interface RequireAccountAccess {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityService.hasVetPermission(#accountId, T(com.krazykritterranch.rms.model.user.VetPermissionType).VIEW_LIVESTOCK)")
    public @interface RequireVetViewPermission {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@securityService.hasVetPermission(#accountId, T(com.krazykritterranch.rms.model.user.VetPermissionType).EDIT_LIVESTOCK)")
    public @interface RequireVetEditPermission {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ADMIN')")
    public @interface RequireAdmin {}
}