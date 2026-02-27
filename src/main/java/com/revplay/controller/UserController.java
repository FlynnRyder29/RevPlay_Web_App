package com.revplay.controller;

import com.revplay.dto.UserDTO;
import com.revplay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserDTO user = userService.getUserProfile(userDetails.getUsername());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) String bio,
            RedirectAttributes redirectAttributes) {

        try {
            userService.updateProfile(userDetails.getUsername(), displayName, bio);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            log.info("Profile updated for: {}", userDetails.getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile.");
            log.error("Profile update failed: {}", e.getMessage());
        }

        return "redirect:/user/profile";
    }
}
