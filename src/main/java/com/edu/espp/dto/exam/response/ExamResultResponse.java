package com.edu.espp.dto.exam.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamResultResponse {
    private Double score;
    private Integer correctAnswersCount;
    private Integer totalQuestions;
    private Integer timeSpent;
}
