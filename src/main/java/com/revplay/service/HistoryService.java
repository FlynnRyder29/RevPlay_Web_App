package com.revplay.service;

import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.ListeningHistory;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.HistoryRepository;
import com.revplay.repository.SongRepository;
import com.revplay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoryService {

    private static final Logger log =
            LoggerFactory.getLogger(HistoryService.class);

    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    public HistoryService(HistoryRepository historyRepository,
                          UserRepository userRepository,
                          SongRepository songRepository) {
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.songRepository = songRepository;
    }

    // -------------------------
    // Helper: Current User
    // -------------------------

    private User getCurrentUser() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User", "username", username));
    }

    // -------------------------
    // ADD TO HISTORY
    // -------------------------

    @Transactional
    public void addToHistory(Long songId) {

        User currentUser = getCurrentUser();

        Song song = songRepository.findById(songId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Song", "id", songId));

        ListeningHistory history = new ListeningHistory();
        history.setUser(currentUser);
        history.setSong(song);

        historyRepository.save(history);

        log.debug("Added history: user={}, song={}",
                currentUser.getId(), songId);
    }

    // -------------------------
    // GET MY HISTORY (PAGINATED)
    // -------------------------

    public Page<ListeningHistory> getMyHistory(int limit) {

        User currentUser = getCurrentUser();

        // Safety guard: prevent negative or zero limits
        if (limit <= 0) {
            limit = 50;
        }

        Pageable pageable = PageRequest.of(0, limit);

        log.debug("Fetching history for user {} with limit {}",
                currentUser.getId(), limit);

        return historyRepository
                .findByUser_IdOrderByPlayedAtDesc(
                        currentUser.getId(),
                        pageable
                );
    }

    // -------------------------
    // CLEAR HISTORY
    // -------------------------

    @Transactional
    public void clearHistory() {

        User currentUser = getCurrentUser();

        historyRepository.deleteByUser_Id(currentUser.getId());

        log.debug("Cleared history for user {}",
                currentUser.getId());
    }
}