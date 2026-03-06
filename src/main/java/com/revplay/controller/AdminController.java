package com.revplay.controller;

import com.revplay.model.Role;
import com.revplay.service.AdminService;
import com.revplay.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;
    private final SecurityUtils securityUtils;

    // ── Dashboard Stats ──

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("GET /api/admin/stats");
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/stats/roles")
    public ResponseEntity<Map<String, Long>> getRoleDistribution() {
        log.info("GET /api/admin/stats/roles");
        return ResponseEntity.ok(adminService.getRoleDistribution());
    }

    @GetMapping("/stats/growth")
    public ResponseEntity<?> getUserGrowth(@RequestParam(defaultValue = "30") int days) {
        log.info("GET /api/admin/stats/growth?days={}", days);
        return ResponseEntity.ok(adminService.getNewUsersPerDay(days));
    }

    @GetMapping("/stats/top-songs")
    public ResponseEntity<?> getTopSongs(@RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/admin/stats/top-songs?limit={}", limit);
        return ResponseEntity.ok(adminService.getTopSongs(limit));
    }

    // ── User Management ──

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/admin/users search={} role={} page={}", search, role, page);
        return ResponseEntity.ok(adminService.getUsers(search, role, page, size));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        log.info("GET /api/admin/users/{}", id);
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    // ── Artist Requests ── (add to existing AdminController)

    @GetMapping("/artist-requests/stats")
    public ResponseEntity<?> getArtistRequestStats() {
        log.info("GET /api/admin/artist-requests/stats");
        return ResponseEntity.ok(adminService.getArtistRequestStats());
    }

    @GetMapping("/artist-requests")
    public ResponseEntity<?> getArtistRequests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/admin/artist-requests status={} page={}", status, page);
        return ResponseEntity.ok(adminService.getArtistRequests(status, page, size));
    }

    @PutMapping("/artist-requests/{id}/approve")
    public ResponseEntity<?> approveArtistRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        Long adminId = securityUtils.getCurrentUser().getId();
        String note = (body != null) ? body.get("note") : null;

        log.info("PUT /api/admin/artist-requests/{}/approve by admin {}", id, adminId);

        try {
            return ResponseEntity.ok(adminService.approveArtistRequest(id, adminId, note));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/artist-requests/{id}/reject")
    public ResponseEntity<?> rejectArtistRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        Long adminId = securityUtils.getCurrentUser().getId();
        String note = (body != null) ? body.get("note") : null;

        log.info("PUT /api/admin/artist-requests/{}/reject by admin {}", id, adminId);

        try {
            return ResponseEntity.ok(adminService.rejectArtistRequest(id, adminId, note));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String newRoleStr = body.get("role");
        if (newRoleStr == null || newRoleStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Role is required"));
        }

        // Prevent admin from changing their own role
        Long currentUserId = securityUtils.getCurrentUser().getId();
        if (currentUserId.equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot change your own role"));
        }

        try {
            Role newRole = Role.valueOf(newRoleStr.toUpperCase());
            log.info("PUT /api/admin/users/{}/role → {}", id, newRole);
            return ResponseEntity.ok(adminService.changeUserRole(id, newRole));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + newRoleStr));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        // Prevent admin from deleting themselves
        Long currentUserId = securityUtils.getCurrentUser().getId();
        if (currentUserId.equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete your own account"));
        }

        log.info("DELETE /api/admin/users/{}", id);

        try {
            adminService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Content Moderation ──

    @DeleteMapping("/songs/{id}")
    public ResponseEntity<?> deleteSong(@PathVariable Long id) {
        log.info("DELETE /api/admin/songs/{}", id);
        adminService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/playlists/{id}")
    public ResponseEntity<?> deletePlaylist(@PathVariable Long id) {
        log.info("DELETE /api/admin/playlists/{}", id);
        adminService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }
}