package com.krazykritterranch.rms.viewController;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String getHomePage(ModelMap model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            String username = userDetails.getUsername();
            List<GrantedAuthority> roles = userDetails.getAuthorities().stream().collect(Collectors.toList());
            model.addAttribute("username", username);
            model.addAttribute("roles", roles);
        }
        return "home_page";
    }
}
