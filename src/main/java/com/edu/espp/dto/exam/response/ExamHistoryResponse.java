package com.edu.espp.dto.exam.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data@AllArgsConstructor@NoArgsConstructor@Builder
public class ExamHistoryResponse {
    private Long id;
    private Long examId;
    private String examTitle;
    private Double score;
    private Integer correctAnswersCount;
    private Integer timeSpent;
    private LocalDateTime testedAt;
}
