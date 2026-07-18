package com.edu.espp.dto.profile;

import com.edu.espp.dto.exam.response.ExamHistoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chứa toàn bộ dữ liệu cần hiển thị
 * tại trang Profile & Progress của học viên.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileProgressResponse {

    // Thông tin cá nhân
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    // Thống kê kết quả thi
    private int totalExams;
    private double averageScore;
    private double highestScore;
    private double latestScore;

    // Danh sách lịch sử thi
    private List<ExamHistoryResponse> examHistories;
}