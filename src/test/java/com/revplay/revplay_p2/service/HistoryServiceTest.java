package com.revplay.revplay_p2.service;

import com.revplay.revplay_p2.model.ListeningHistory;
import com.revplay.revplay_p2.repository.HistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HistoryServiceTest {

    @Mock
    private HistoryRepository historyRepository;

    @InjectMocks
    private HistoryService historyService;

    @Test
    void recordPlay_ShouldSaveHistory() {
        ListeningHistory history = new ListeningHistory();
        history.setUserId(1L);
        history.setSongId(101L);
        when(historyRepository.save(any(ListeningHistory.class))).thenAnswer(invocation -> {
            ListeningHistory h = invocation.getArgument(0);
            h.setId(1L);
            return h;
        });

        ListeningHistory result = historyService.recordPlay(1L, 101L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(101L, result.getSongId());
        assertNotNull(result.getPlayedAt());
        verify(historyRepository, times(1)).save(any(ListeningHistory.class));
    }

    @Test
    void getHistory_WithLimit_ShouldReturnLimitedList() {
        ListeningHistory h1 = new ListeningHistory(); h1.setId(1L);
        ListeningHistory h2 = new ListeningHistory(); h2.setId(2L);
        ListeningHistory h3 = new ListeningHistory(); h3.setId(3L);
        List<ListeningHistory> fullList = List.of(h1, h2, h3);
        when(historyRepository.findByUserIdOrderByPlayedAtDesc(1L)).thenReturn(fullList);

        List<ListeningHistory> result = historyService.getHistory(1L, 2);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void getHistory_WithoutLimit_ShouldReturnAll() {
        ListeningHistory h1 = new ListeningHistory(); h1.setId(1L);
        ListeningHistory h2 = new ListeningHistory(); h2.setId(2L);
        List<ListeningHistory> fullList = List.of(h1, h2);
        when(historyRepository.findByUserIdOrderByPlayedAtDesc(1L)).thenReturn(fullList);

        List<ListeningHistory> result = historyService.getHistory(1L, null);

        assertEquals(2, result.size());
    }

    @Test
    void getAllHistory_ShouldReturnAll() {
        ListeningHistory h1 = new ListeningHistory(); h1.setId(1L);
        ListeningHistory h2 = new ListeningHistory(); h2.setId(2L);
        when(historyRepository.findByUserIdOrderByPlayedAtDesc(1L)).thenReturn(List.of(h1, h2));

        List<ListeningHistory> result = historyService.getAllHistory(1L);

        assertEquals(2, result.size());
    }

    @Test
    void clearHistory_ShouldCallDeleteByUserId() {
        doNothing().when(historyRepository).deleteByUserId(1L);

        historyService.clearHistory(1L);

        verify(historyRepository, times(1)).deleteByUserId(1L);
    }
}