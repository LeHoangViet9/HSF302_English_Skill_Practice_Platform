package com.edu.espp.dto;

import com.edu.espp.entity.ExamHistory;
import com.edu.espp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminDashboardData {

    private String adminName;
    private long totalUsers;
    private long totalStudents;
    private long totalStaff;
    private long totalAdmins;

    private long totalExams;
    private long totalExamAttempts;
    private double systemAverageScore;

    private List<User> recentUserRegistrations;
    private List<ExamHistory> recentSystemExams;
}
