package com.edu.espp.dto;

import com.edu.espp.dto.lesson.response.LessonResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StaffDashboardData {

    private String staffName;
    private long totalLessons;
    private long publishedLessons;
    private long totalContents;
    private long totalQuestions;
    private long lessonsNeedingContent;
    private List<LessonResponse> recentLessons;
}
