package com.edu.espp.dto.exam.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamSubmitRequest {
    @NotNull(message = "Câu trả lời không được để trống")
    private Map<Long, String> userAnswers;
    @NotNull(message = "Thời gian làm bài không được để trống")
    private Integer timeSpent;
}
