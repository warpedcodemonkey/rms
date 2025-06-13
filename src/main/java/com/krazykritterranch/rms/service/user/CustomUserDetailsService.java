package com.krazykritterranch.rms.service.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.krazykritterranch.rms.model.user.User byLogin = userService.findByLogin(username);
        if (byLogin == null) {
            return null;
        }
        return User.builder()
                .username(byLogin.getUserName())
                .password(byLogin.getPassWord())
                .roles(byLogin.getRoles().toString())
                .build();
    }

}
