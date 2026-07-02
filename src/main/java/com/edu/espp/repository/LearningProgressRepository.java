
package com.edu.espp.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.edu.espp.entity.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    long countByUser_Id(Long userId);

    long countByUser_IdAndIsCompletedTrue(Long userId);

    Optional<LearningProgress> findTopByUser_IdOrderByUpdatedAtDesc(Long userId);

    Optional<LearningProgress> findTopByUser_IdAndIsCompletedFalseOrderByUpdatedAtDesc(Long userId);
    long countByUserId(Long userId);

    Optional<LearningProgress> findTopByUserIdOrderByUpdatedAtDesc(Long userId);

    long countByUserIdAndIsCompletedTrue(Long userId);

    Optional<LearningProgress> findTopByUserIdAndIsCompletedFalseOrderByUpdatedAtDesc(Long userId);
}