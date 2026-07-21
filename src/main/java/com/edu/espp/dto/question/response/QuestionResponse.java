package com.edu.espp.dto.question.response;

import com.edu.espp.common.enums.QuestionSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {
    private Long id;
    private Long examId;
    private String examTitle;
    private QuestionSkill skill;
    private String questionText;
    private String audioUrl;
    private Map<String, String> options;
    private String correctAnswer;
    private String explanation;
}
