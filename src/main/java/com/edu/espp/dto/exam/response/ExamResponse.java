package com.edu.espp.dto.exam.response;

import com.edu.espp.common.enums.TypeQuiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResponse {
    private Long id;

    private String title;

    private TypeQuiz type; // QUIZ, MOCK_TEST

    private Integer duration; // tính bằng phút
    private Integer totalQuestions;
    private String description;
    
    private com.edu.espp.common.enums.ApprovalStatus approvalStatus;
    private String rejectReason;
}
