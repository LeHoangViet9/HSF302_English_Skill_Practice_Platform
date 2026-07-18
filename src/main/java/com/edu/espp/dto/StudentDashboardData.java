package com.edu.espp.dto;

import com.edu.espp.entity.Lesson;
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

    private Lesson recentLesson;
    private Lesson suggestedLesson;

    private long totalExamsTaken;
    private double averageExamScore;
    private com.edu.espp.entity.ExamHistory recentExam;

    private List<SRSReview> upcomingReviews;
}