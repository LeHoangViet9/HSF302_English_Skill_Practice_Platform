package com.edu.espp.service;

import com.edu.espp.dto.StudentDashboardData;
import com.edu.espp.entity.LearningProgress;
import com.edu.espp.entity.Lesson;
import com.edu.espp.entity.SRSReview;
import com.edu.espp.repository.LearningProgressRepository;
import com.edu.espp.repository.LessonRepository;
import com.edu.espp.repository.SRSReviewRepository;
import com.edu.espp.repository.UserRepository;
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
    private final com.edu.espp.repository.ExamHistoryRepository examHistoryRepository;

    @Transactional(readOnly = true)
    public StudentDashboardData getStudentDashboard(Long userId) {
        long totalFlashcards = srsReviewRepository.countByUser_Id(userId);

        long dueFlashcardsToday = srsReviewRepository
                .countByUser_IdAndNextReviewDateLessThanEqual(userId, LocalDateTime.now());

        long learnedLessons = learningProgressRepository.countByUserId(userId);

        long completedLessons = learningProgressRepository.countByUserIdAndIsCompletedTrue(userId);

        long totalLessons = lessonRepository.count();

        // Ép kiểu chuẩn để tránh lỗi chia nguyên (Integer Division) ra 0
        double completionPercent = totalLessons == 0
                ? 0
                : (completedLessons * 100.0) / totalLessons;

        Lesson recentLesson = learningProgressRepository
                .findTopByUserIdOrderByUpdatedAtDesc(userId)
                .map(LearningProgress::getLesson)
                .orElse(null);

        Lesson suggestedLesson = findSuggestedLesson(userId, recentLesson);

        List<SRSReview> upcomingReviews = srsReviewRepository
                .findTop5ByUser_IdOrderByNextReviewDateAsc(userId);

        String studentName = userRepository.findById(userId)
                .map(user -> user.getFullName())
                .orElse("Học viên");

        List<com.edu.espp.entity.ExamHistory> examHistories = examHistoryRepository.findByUserIdOrderByTestedAtDesc(userId);
        long totalExamsTaken = examHistories.size();
        double averageExamScore = totalExamsTaken == 0 ? 0 : examHistories.stream().mapToDouble(h -> h.getScore() != null ? h.getScore() : 0.0).average().orElse(0.0);
        com.edu.espp.entity.ExamHistory recentExam = totalExamsTaken > 0 ? examHistories.get(0) : null;

        return new StudentDashboardData(
                studentName,
                totalFlashcards,
                dueFlashcardsToday,
                learnedLessons,
                completedLessons,
                completionPercent,
                recentLesson,
                suggestedLesson,
                totalExamsTaken,
                averageExamScore,
                recentExam,
                upcomingReviews
        );
    }

    // Tách riêng logic xử lý gợi ý bài học mượt mà
    private Lesson findSuggestedLesson(Long userId, Lesson recentLesson) {
        return learningProgressRepository
                .findTopByUserIdAndIsCompletedFalseOrderByUpdatedAtDesc(userId)
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