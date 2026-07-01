package com.edu.espp.repository;

import com.edu.espp.entity.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository; // Đảm bảo có extends thằng này nhé m
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {
    long countByUserId(Long userId);

    long countByUserIdAndIsCompletedTrue(Long userId);

    Optional<LearningProgress> findTopByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<LearningProgress> findTopByUserIdAndIsCompletedFalseOrderByUpdatedAtDesc(Long userId);
}