package com.edu.espp.service;


import com.edu.espp.common.enums.ReviewResult;
import com.edu.espp.entity.LessonContent;
import com.edu.espp.entity.SRSReview;
import com.edu.espp.entity.User;
import com.edu.espp.repository.LessonContentRepository;
import com.edu.espp.repository.SRSReviewRepository;
import com.edu.espp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SRSReviewService {

    private final SRSReviewRepository srsReviewRepository;
    private final UserRepository userRepository;
    private final LessonContentRepository lessonContentRepository;

    public List<SRSReview> getDueReviews(Long userId) {
        return srsReviewRepository.findByUser_IdAndNextReviewDateLessThanEqual(
                userId,
                LocalDateTime.now()
        );
    }

    public void reviewFlashcard(Long userId, Long contentId, ReviewResult result) {
        SRSReview review = srsReviewRepository
                .findByUser_IdAndContent_Id(userId, contentId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

                    LessonContent content = lessonContentRepository.findById(contentId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy lesson content"));

                    return SRSReview.builder()
                            .user(user)
                            .content(content)
                            .build();
                });

        double easeFactor = review.getEaseFactor();
        int repetition = review.getRepetition();
        int intervalDays = review.getSrsInterval();

        switch (result) {
            case AGAIN -> {
                repetition = 0;
                intervalDays = 1;
                easeFactor = Math.max(1.3, easeFactor - 0.2);
            }
            case HARD -> {
                intervalDays = Math.max(1, (int) Math.round(intervalDays * 1.2));
                easeFactor = Math.max(1.3, easeFactor - 0.15);
            }
            case GOOD -> {
                if (repetition == 0) {
                    intervalDays = 1;
                } else if (repetition == 1) {
                    intervalDays = 6;
                } else {
                    intervalDays = (int) Math.round(intervalDays * easeFactor);
                }
                repetition++;
            }
            case EASY -> {
                if (repetition == 0) {
                    intervalDays = 4;
                } else {
                    intervalDays = (int) Math.round(intervalDays * easeFactor * 1.3);
                }
                easeFactor += 0.15;
                repetition++;
            }
        }

        review.setEaseFactor(easeFactor);
        review.setRepetition(repetition);
        review.setSrsInterval(intervalDays);
        review.setNextReviewDate(LocalDateTime.now().plusDays(intervalDays));

        srsReviewRepository.save(review);
    }
}