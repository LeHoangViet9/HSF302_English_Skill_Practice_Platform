package com.edu.espp.dto;

import com.edu.espp.dto.lesson.response.LessonResponse;
import com.edu.espp.entity.ExamHistory;
import com.edu.espp.entity.SRSReview;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDashboardData {

    private String studentName;

    private long totalFlashcards;
    private long dueFlashcardsToday;

    private long learnedLessons;
    private long completedLessons;
    private double completionPercent;

    private LessonResponse recentLesson;
    private LessonResponse suggestedLesson;

    private long totalExamsTaken;
    private double averageExamScore;
    private ExamHistory recentExam;

    private List<SRSReview> upcomingReviews;
}
