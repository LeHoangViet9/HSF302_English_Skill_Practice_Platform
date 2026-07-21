package com.edu.espp.dto;

import com.edu.espp.entity.Lesson;
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
    private List<Lesson> recentLessons;
}
