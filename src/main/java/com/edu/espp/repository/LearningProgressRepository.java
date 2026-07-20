
package com.edu.espp.repository;

import com.edu.espp.entity.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    long countByUser_Id(Long userId);

    long countByUser_IdAndIsCompletedTrue(Long userId);

    Optional<LearningProgress> findTopByUser_IdOrderByUpdatedAtDesc(Long userId);

    Optional<LearningProgress> findTopByUser_IdAndIsCompletedFalseOrderByUpdatedAtDesc(Long userId);

    Optional<LearningProgress> findByUser_IdAndLesson_Id(Long userId, Long lessonId);

    boolean existsByUser_IdAndLesson_IdAndIsCompletedTrue(Long userId, Long lessonId);

    List<LearningProgress> findByUser_IdAndIsCompletedTrue(Long userId);
}
