package com.edu.espp.Repository;

import com.edu.espp.entity.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    long countByUser_Id(Long userId);

    long countByUser_IdAndIsCompletedTrue(Long userId);

    Optional<LearningProgress> findTopByUser_IdOrderByUpdatedAtDesc(Long userId);

    Optional<LearningProgress> findTopByUser_IdAndIsCompletedFalseOrderByUpdatedAtDesc(Long userId);
}