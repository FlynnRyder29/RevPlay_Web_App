package com.revplay.repository;

import com.revplay.model.ArtistRequest;
import com.revplay.model.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRequestRepository extends JpaRepository<ArtistRequest, Long> {

    // Latest request by user (any status)
    Optional<ArtistRequest> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    // Check if user has a pending request
    boolean existsByUserIdAndStatus(Long userId, RequestStatus status);

    // All requests by status
    Page<ArtistRequest> findByStatus(RequestStatus status, Pageable pageable);

    // All requests (paginated)
    Page<ArtistRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Count by status
    long countByStatus(RequestStatus status);

    // All requests for a user
    List<ArtistRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
}