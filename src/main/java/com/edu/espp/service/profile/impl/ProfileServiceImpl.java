package com.edu.espp.service.profile.impl;

import com.edu.espp.dto.exam.response.ExamHistoryResponse;
import com.edu.espp.dto.profile.ProfileProgressResponse;
import com.edu.espp.entity.User;
import com.edu.espp.repository.UserRepository;
import com.edu.espp.service.history.ExamHistoryService;
import com.edu.espp.service.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Triển khai xử lý dữ liệu Profile & Progress.
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final ExamHistoryService examHistoryService;

    @Override
    @Transactional(readOnly = true)
    public ProfileProgressResponse getProfileProgress(Long userId) {

        /*
         * Bước 1:
         * Tìm người dùng trong bảng users.
         */
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy người dùng có ID: " + userId
                        )
                );

        /*
         * Bước 2:
         * Lấy lịch sử thi của người dùng.
         *
         * Service có sẵn trong project:
         * ExamHistoryService.getUserExamHistory(userId)
         */
        List<ExamHistoryResponse> examHistories =
                examHistoryService.getUserExamHistory(userId);

        /*
         * Bước 3:
         * Tính tổng số bài thi.
         */
        int totalExams = examHistories.size();

        /*
         * Bước 4:
         * Tính điểm trung bình.
         *
         * Những lịch sử có score = null sẽ bị bỏ qua.
         */
        double averageScore = examHistories.stream()
                .filter(history -> history.getScore() != null)
                .mapToDouble(ExamHistoryResponse::getScore)
                .average()
                .orElse(0.0);

        /*
         * Bước 5:
         * Tìm điểm cao nhất.
         */
        double highestScore = examHistories.stream()
                .filter(history -> history.getScore() != null)
                .mapToDouble(ExamHistoryResponse::getScore)
                .max()
                .orElse(0.0);

        /*
         * Repository đang sắp xếp testedAt giảm dần,
         * nên phần tử đầu tiên là bài thi gần nhất.
         */
        double latestScore = 0.0;

        if (!examHistories.isEmpty()
                && examHistories.get(0).getScore() != null) {

            latestScore = examHistories.get(0).getScore();
        }

        /*
         * Bước 6:
         * Đưa toàn bộ dữ liệu vào DTO.
         */
        return ProfileProgressResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole() != null
                        ? user.getRole().name()
                        : "STUDENT")
                .status(user.getStatus() != null
                        ? user.getStatus().name()
                        : "UNKNOWN")
                .createdAt(user.getCreatedAt())
                .totalExams(totalExams)
                .averageScore(roundScore(averageScore))
                .highestScore(roundScore(highestScore))
                .latestScore(roundScore(latestScore))
                .examHistories(examHistories)
                .build();
    }

    /**
     * Làm tròn điểm đến hai chữ số thập phân.
     *
     * Ví dụ:
     * 7.666666 -> 7.67
     */
    private double roundScore(double score) {
        return Math.round(score * 100.0) / 100.0;
    }
}