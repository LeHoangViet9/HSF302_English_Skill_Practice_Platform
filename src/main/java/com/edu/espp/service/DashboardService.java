package com.edu.espp.service;

import com.edu.espp.common.enums.ApprovalStatus;
import com.edu.espp.common.enums.Role;
import com.edu.espp.dto.AdminDashboardData;
import com.edu.espp.dto.StaffDashboardData;
import com.edu.espp.dto.StudentDashboardData;
import com.edu.espp.entity.ExamHistory;
import com.edu.espp.entity.LearningProgress;
import com.edu.espp.entity.Lesson;
import com.edu.espp.entity.SRSReview;
import com.edu.espp.entity.User;
import com.edu.espp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SRSReviewRepository srsReviewRepository;
    private final SRSReviewService srsReviewService;
    private final BookMarkRepository bookMarkRepository;
    private final LessonRepository lessonRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final UserRepository userRepository;
    private final ExamHistoryRepository examHistoryRepository;
    private final LessonContentRepository lessonContentRepository;
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;

    @Transactional(readOnly = true)
    public StudentDashboardData getStudentDashboard(Long userId) {
        srsReviewService.syncReviewsWithBookmarks(userId);

        long totalFlashcards = bookMarkRepository.countByUser_Id(userId);

        long dueFlashcardsToday = srsReviewRepository
                .countByUser_IdAndNextReviewDateLessThanEqual(userId, LocalDateTime.now());

        long learnedLessons = learningProgressRepository.countByUser_Id(userId);

        long completedLessons = learningProgressRepository.countByUser_IdAndIsCompletedTrue(userId);

        long totalLessons = lessonRepository.count();

        double completionPercent = totalLessons == 0
                ? 0
                : (completedLessons * 100.0) / totalLessons;

        Lesson recentLesson = learningProgressRepository
                .findTopByUserIdOrderByUpdatedAtDesc(userId)
                .map(LearningProgress::getLesson)
                .orElse(null);

        Lesson suggestedLesson = findSuggestedLesson(userId, recentLesson);

        List<SRSReview> upcomingReviews = srsReviewService.getUpcomingReviews(userId);

        String studentName = userRepository.findById(userId)
                .map(User::getFullName)
                .orElse("Học viên");

        List<ExamHistory> examHistories = examHistoryRepository.findByUserIdOrderByTestedAtDesc(userId);
        long totalExamsTaken = examHistories.size();
        double averageExamScore = totalExamsTaken == 0 ? 0 : examHistories.stream().mapToDouble(h -> h.getScore() != null ? h.getScore() : 0.0).average().orElse(0.0);
        ExamHistory recentExam = totalExamsTaken > 0 ? examHistories.get(0) : null;

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

    @Transactional(readOnly = true)
    public StaffDashboardData getStaffDashboard(Long staffId) {
        String staffName = userRepository.findById(staffId)
                .map(User::getFullName)
                .orElse("Staff");

        long totalLessons = lessonRepository.count();
        long publishedLessons = lessonRepository.countByApprovalStatus(ApprovalStatus.APPROVED);
        long totalContents = lessonContentRepository.count();
        long totalQuestions = questionRepository.count();
        List<Lesson> recentLessons = lessonRepository.findTop5ByOrderByIdDesc();

        long lessonsNeedingContent = lessonRepository.countByApprovalStatus(ApprovalStatus.PENDING)
                + lessonRepository.countByApprovalStatus(ApprovalStatus.REJECTED);

        return StaffDashboardData.builder()
                .staffName(staffName)
                .totalLessons(totalLessons)
                .publishedLessons(publishedLessons)
                .totalContents(totalContents)
                .totalQuestions(totalQuestions)
                .lessonsNeedingContent(lessonsNeedingContent)
                .recentLessons(recentLessons)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminDashboardData getAdminDashboard(Long adminId) {
        String adminName = userRepository.findById(adminId)
                .map(User::getFullName)
                .orElse("Administrator");

        long totalUsers = userRepository.count();
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long totalStaff = userRepository.countByRole(Role.STAFF);
        long totalAdmins = userRepository.countByRole(Role.ADMIN);

        long totalExams = examRepository.count();
        long totalExamAttempts = examHistoryRepository.count();

        List<ExamHistory> allHistories = examHistoryRepository.findAll();
        double systemAverageScore = allHistories.isEmpty() ? 0.0 :
                allHistories.stream().mapToDouble(h -> h.getScore() != null ? h.getScore() : 0.0).average().orElse(0.0);

        List<User> recentUsers = userRepository.findTop5ByOrderByIdDesc();
        List<ExamHistory> recentSystemExams = examHistoryRepository.findTop5ByOrderByTestedAtDesc();

        return AdminDashboardData.builder()
                .adminName(adminName)
                .totalUsers(totalUsers)
                .totalStudents(totalStudents)
                .totalStaff(totalStaff)
                .totalAdmins(totalAdmins)
                .totalExams(totalExams)
                .totalExamAttempts(totalExamAttempts)
                .systemAverageScore(systemAverageScore)
                .recentUserRegistrations(recentUsers)
                .recentSystemExams(recentSystemExams)
                .build();
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