package com.edu.espp.dto.question.request;

import com.edu.espp.common.enums.QuestionSkill;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequest {
    @NotNull(message = "Mã đề không được để trống")
    private Long examId;
    @NotNull(message = "Kỹ năng không được để trống")
    private QuestionSkill skill;
    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String questionText;
    @NotBlank(message = "URL âm thanh không được để trống")
    private String audioUrl;
    @NotBlank(message = "Lựa chọn không được để trống")
    private String options;
    @NotBlank(message = "Đáp án đúng không được để trống")
    private String correctAnswer;
    @NotBlank(message = "Giải thích không được để trống")
    private String explanation;
}
