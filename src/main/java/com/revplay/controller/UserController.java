package com.revplay.controller;

import com.revplay.dto.UserDTO;
import com.revplay.service.FavoriteService;
import com.revplay.service.FileStorageService;
import com.revplay.service.UserService;
import com.revplay.repository.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final FavoriteService favoriteService;
    private final PlaylistRepository playlistRepository;

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserDTO user = userService.getUserProfile(userDetails.getUsername());
        model.addAttribute("user", user);

        // Format the joined date for display
        if (user.getCreatedAt() != null) {
            String joinedDate = user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM yyyy"));
            model.addAttribute("joinedDate", joinedDate);
        }

        // User stats (Day 8)
        try {
            int favCount = favoriteService.getMyFavoriteSongIds().size();
            long playlistCount = playlistRepository.countByUser_Id(user.getId());
            model.addAttribute("favCount", favCount);
            model.addAttribute("playlistCount", playlistCount);
        } catch (Exception e) {
            model.addAttribute("favCount", 0);
            model.addAttribute("playlistCount", 0);
        }

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

    // -------------------------
    // PROFILE PICTURE UPLOAD (Day 8)
    // -------------------------

    @PostMapping("/profile/picture")
    public String uploadProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("profilePicture") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
                return "redirect:/user/profile";
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Only image files are allowed.");
                return "redirect:/user/profile";
            }

            // Store file and get relative path
            String filePath = fileStorageService.storeFile(file, "profile-pictures");

            // Save URL to user profile
            userService.updateProfilePicture(userDetails.getUsername(), "/uploads/" + filePath);

            redirectAttributes.addFlashAttribute("success", "Profile picture updated!");
            log.info("Profile picture uploaded for: {}", userDetails.getUsername());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload picture.");
            log.error("Profile picture upload failed: {}", e.getMessage());
        }

        return "redirect:/user/profile";
    }
}
