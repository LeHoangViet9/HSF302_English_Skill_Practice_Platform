package com.edu.espp.dto.exam.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamAttemptDetailResponse {
    private Long questionId; // Hãy chắc chắn trường này là câu chữ 'questionId' kiểu Long
    private String questionText;
    private List<String> options;
    private String correctAnswer;
    private String selectedAnswer;
    private Boolean isCorrect;
    private String explanation;
}
