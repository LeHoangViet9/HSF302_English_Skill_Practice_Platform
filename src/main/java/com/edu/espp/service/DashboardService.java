package com.edu.espp.service;

import com.edu.espp.Repository.LearningProgressRepository;
import com.edu.espp.Repository.LessonRepository;
import com.edu.espp.Repository.SRSReviewRepository;
import com.edu.espp.Repository.UserRepository;
import com.edu.espp.dto.StudentDashboardData;
import com.edu.espp.entity.LearningProgress;
import com.edu.espp.entity.Lesson;
import com.edu.espp.entity.SRSReview;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SRSReviewRepository srsReviewRepository;
    private final LessonRepository lessonRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public StudentDashboardData getStudentDashboard(Long userId) {
        long totalFlashcards = srsReviewRepository.countByUser_Id(userId);

        long dueFlashcardsToday = srsReviewRepository
                .countByUser_IdAndNextReviewDateLessThanEqual(userId, LocalDateTime.now());

        long learnedLessons = learningProgressRepository.countByUser_Id(userId);

        long completedLessons = learningProgressRepository.countByUser_IdAndIsCompletedTrue(userId);

        long totalLessons = lessonRepository.count();

        double completionPercent = totalLessons == 0
                ? 0
                : (completedLessons * 100.0) / totalLessons;

        Lesson recentLesson = learningProgressRepository
                .findTopByUser_IdOrderByUpdatedAtDesc(userId)
                .map(LearningProgress::getLesson)
                .orElse(null);

        Lesson suggestedLesson = findSuggestedLesson(userId, recentLesson);

        List<SRSReview> upcomingReviews = srsReviewRepository
                .findTop5ByUser_IdOrderByNextReviewDateAsc(userId);

        String studentName = userRepository.findById(userId)
                .map(user -> user.getFullName())
                .orElse("Học viên");

        return new StudentDashboardData(
                studentName,
                totalFlashcards,
                dueFlashcardsToday,
                learnedLessons,
                completedLessons,
                completionPercent,
                recentLesson,
                suggestedLesson,
                upcomingReviews
        );
    }

    private Lesson findSuggestedLesson(Long userId, Lesson recentLesson) {
        return learningProgressRepository
                .findTopByUser_IdAndIsCompletedFalseOrderByUpdatedAtDesc(userId)
                .map(LearningProgress::getLesson)
                .orElseGet(() -> {
                    if (recentLesson != null) {
                        return lessonRepository
                                .findFirstByIdGreaterThanOrderByIdAsc(recentLesson.getId())
                                .orElseGet(() -> lessonRepository.findFirstByOrderByIdAsc().orElse(null));
                    }

                    return lessonRepository.findFirstByOrderByIdAsc().orElse(null);
                });
    }
}