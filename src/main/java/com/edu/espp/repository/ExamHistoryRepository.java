package com.edu.espp.repository;

import com.edu.espp.entity.ExamHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamHistoryRepository extends JpaRepository<ExamHistory, Long> {
    List<ExamHistory> findByUserIdOrderByTestedAtDesc(Long userId);
    Page<ExamHistory> findByUserId(Long userId, Pageable pageable);
    
    List<ExamHistory> findAllByOrderByTestedAtDesc();

    List<ExamHistory> findTop5ByOrderByTestedAtDesc();
}
