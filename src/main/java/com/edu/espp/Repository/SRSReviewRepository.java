package com.edu.espp.Repository;

import com.edu.espp.entity.SRSReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SRSReviewRepository extends JpaRepository<SRSReview, Long> {
    List<SRSReview> findByUser_IdAndNextReviewDateLessThanEqual(Long userId, LocalDateTime now);

    Optional<SRSReview> findByUser_IdAndContent_Id(Long userId, Long contentId);

    long countByUser_Id(Long userId);

    long countByUser_IdAndNextReviewDateLessThanEqual(
            Long userId,
            LocalDateTime now
    );

    List<SRSReview> findTop5ByUser_IdOrderByNextReviewDateAsc(Long userId);


}
