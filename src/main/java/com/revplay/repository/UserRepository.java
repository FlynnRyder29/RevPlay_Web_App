package com.revplay.repository;

import com.revplay.model.Role;
import com.revplay.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailOrUsername(String email, String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // ── Admin queries ──

    long countByRole(Role role);

    long countByCreatedAtAfter(LocalDateTime after);

    Page<User> findByRole(Role role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<User> searchUsers(@Param("q") String query, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND (" +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<User> searchUsersByRole(@Param("q") String query, @Param("role") Role role, Pageable pageable);

    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countByRoleGrouped();

    @Query("SELECT DATE(u.createdAt), COUNT(u) FROM User u " +
            "WHERE u.createdAt >= :since GROUP BY DATE(u.createdAt) ORDER BY DATE(u.createdAt)")
    List<Object[]> countNewUsersPerDay(@Param("since") LocalDateTime since);
}