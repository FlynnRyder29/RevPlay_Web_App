package com.revplay.service;

import com.revplay.dto.HistoryDTO;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.ListeningHistory;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.HistoryRepository;
import com.revplay.repository.SongRepository;
import com.revplay.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoryService {

        private static final Logger log = LoggerFactory.getLogger(HistoryService.class);

        private final HistoryRepository historyRepository;
        private final SongRepository songRepository;
        private final SecurityUtils securityUtils;

        public HistoryService(HistoryRepository historyRepository,
                        SongRepository songRepository,
                        SecurityUtils securityUtils) {
                this.historyRepository = historyRepository;
                this.songRepository = songRepository;
                this.securityUtils = securityUtils;
        }

        // -------------------------
        // ADD TO HISTORY
        // -------------------------

        @Transactional
        public void addToHistory(Long songId) {

                User currentUser = securityUtils.getCurrentUser();

                Song song = songRepository.findById(songId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Song", "id", songId));

                ListeningHistory history = new ListeningHistory();
                history.setUser(currentUser);
                history.setSong(song);
                history.setPlayedAt(LocalDateTime.now());
                historyRepository.save(history);

                log.debug("Added history: user={}, song={}",
                                currentUser.getId(), songId);
        }

        // -------------------------
        // GET MY HISTORY (PAGINATED / LIMITED)
        // -------------------------

        @Transactional(readOnly = true)
        public Page<HistoryDTO> getMyHistory(int limit) {

                User currentUser = securityUtils.getCurrentUser();

                // Safety guard: clamp limit to valid range
                if (limit <= 0)
                        limit = 50;
                if (limit > 200)
                        limit = 200;

                Pageable pageable = PageRequest.of(0, limit);

                log.debug("Fetching history for user {} with limit {}",
                                currentUser.getId(), limit);

                return historyRepository
                                .findByUser_IdOrderByPlayedAtDesc(
                                                currentUser.getId(),
                                                pageable)
                                .map(this::toDTO);
        }

        // -------------------------
        // GET ALL HISTORY (complete, unpaginated)
        // -------------------------

        @Transactional(readOnly = true)
        public List<HistoryDTO> getAllHistory() {

                User currentUser = securityUtils.getCurrentUser();

                log.debug("Fetching full history for user {}",
                                currentUser.getId());

                return historyRepository
                                .findAllByUser_IdOrderByPlayedAtDesc(currentUser.getId())
                                .stream()
                                .map(this::toDTO)
                                .toList();
        }

        // -------------------------
        // CLEAR HISTORY
        // -------------------------

        @Transactional
        public void clearHistory() {

                User currentUser = securityUtils.getCurrentUser();

                historyRepository.deleteByUser_Id(currentUser.getId());

                log.debug("Cleared history for user {}",
                                currentUser.getId());
        }

        // -------------------------
        // MAPPER
        // -------------------------

        private HistoryDTO toDTO(ListeningHistory h) {

                HistoryDTO dto = new HistoryDTO();

                dto.setId(h.getId());
                dto.setSongId(h.getSong().getId());
                dto.setSongTitle(h.getSong().getTitle());
                dto.setAudioUrl(h.getSong().getAudioUrl());
                dto.setArtistName(h.getSong().getArtist().getArtistName());
                dto.setCoverImageUrl(h.getSong().getCoverImageUrl());
                dto.setPlayedAt(h.getPlayedAt());

                return dto;
        }
}