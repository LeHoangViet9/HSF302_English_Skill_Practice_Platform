package com.edu.espp.dto.exam.request;

import com.edu.espp.common.enums.TypeQuiz;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamRequest {
    @NotBlank(message = "Tên đề thi không được để trống")
    private String title;
    @NotNull(message = "Loại đề thi không được để trống")
    private TypeQuiz type; // QUIZ, MOCK_TEST
    @NotNull(message = "Thời gian thi không được để trống")
    private Integer duration; // tính bằng phút
    @NotNull(message = "Số lượng câu hỏi không được để trống")
    private Integer totalQuestions;
}
