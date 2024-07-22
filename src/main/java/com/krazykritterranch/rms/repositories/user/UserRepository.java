package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByPhonesFullNumberContaining(String partialPhoneNumber);

    User findByPhonesFullNumber(String phoneNumber);

    User findByUserName(String userName);
}
