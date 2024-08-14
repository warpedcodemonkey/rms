package com.krazykritterranch.rms.viewController;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserViewController {

    @GetMapping("/login")
    public String getLoginPage(ModelMap model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            String username = userDetails.getUsername();
            List<GrantedAuthority> roles = userDetails.getAuthorities().stream().collect(Collectors.toList());
            model.addAttribute("username", username);
            model.addAttribute("roles", roles);
        }
        return "home_page";
    }

    @GetMapping("/logout")
    public String getLogout(ModelMap model, @AuthenticationPrincipal UserDetails userDetails){
        userDetails = null;
        return "home_page";
    }
}
