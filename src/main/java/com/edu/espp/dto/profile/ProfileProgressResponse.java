package com.edu.espp.dto.profile;

import com.edu.espp.dto.exam.response.ExamHistoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chứa dữ liệu hiển thị tại trang hồ sơ và tiến độ của học viên.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileProgressResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    private int totalExams;
    private double averageScore;
    private double highestScore;
    private double latestScore;

    private List<ExamHistoryResponse> examHistories;
}
