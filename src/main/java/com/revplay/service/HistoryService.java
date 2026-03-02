package com.revplay.service;

import com.revplay.dto.HistoryDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.ListeningHistory;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.HistoryRepository;
import com.revplay.repository.SongRepository;
import com.revplay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                                "User",
                                "username",
                                username
                        ));
    }

    // -------------------------
    // RECORD PLAY
    // -------------------------

    @Transactional
    public void addToHistory(Long songId) {

        User currentUser = getCurrentUser();

        Song song = songRepository.findById(songId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Song",
                                "id",
                                songId
                        ));

        ListeningHistory history = new ListeningHistory();
        history.setUser(currentUser);
        history.setSong(song);

        historyRepository.save(history);

        log.debug("Recorded play: user={}, song={}",
                currentUser.getId(), songId);
    }

    // -------------------------
    // GET RECENT HISTORY
    // -------------------------

    public Page<HistoryDTO> getRecentHistory(int limit) {

        User currentUser = getCurrentUser();

        Pageable pageable = PageRequest.of(0, limit);

        Page<ListeningHistory> historyPage =
                historyRepository.findByUser_IdOrderByPlayedAtDesc(
                        currentUser.getId(),
                        pageable
                );

        return historyPage.map(this::toDTO);
    }

    // -------------------------
    // GET ALL HISTORY
    // -------------------------

    public List<HistoryDTO> getAllHistory() {

        User currentUser = getCurrentUser();

        List<ListeningHistory> historyList =
                historyRepository.findByUser_IdOrderByPlayedAtDesc(
                        currentUser.getId()
                );

        return historyList.stream()
                .map(this::toDTO)
                .toList();
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

    // -------------------------
    // MAPPER
    // -------------------------

    private HistoryDTO toDTO(ListeningHistory history) {

        HistoryDTO dto = new HistoryDTO();

        dto.setId(history.getId());
        dto.setSongId(history.getSong().getId());
        dto.setSongTitle(history.getSong().getTitle());
        dto.setPlayedAt(history.getPlayedAt());

        return dto;
    }
}