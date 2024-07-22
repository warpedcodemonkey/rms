package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

}
