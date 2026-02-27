package com.revplay.revplay_p2.repository;

import com.revplay.revplay_p2.model.ListeningHistory;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import java.util.List;

=======
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
>>>>>>> 92cfd1a2bdda491fa75c04f993b7b58af38736c6
public interface HistoryRepository extends JpaRepository<ListeningHistory, Long> {
    List<ListeningHistory> findByUserIdOrderByPlayedAtDesc(Long userId);
    void deleteByUserId(Long userId);
}