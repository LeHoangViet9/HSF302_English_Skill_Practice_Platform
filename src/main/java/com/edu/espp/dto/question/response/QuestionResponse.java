package com.edu.espp.dto.question.response;

import com.edu.espp.common.enums.QuestionSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {
    private Long id;
    private QuestionSkill skill;
    private String questionText;
    private String audioUrl;
    private String options;
    private String explanation;
}
