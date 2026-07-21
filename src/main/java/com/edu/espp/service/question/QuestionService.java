package com.edu.espp.service.question;

import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.dto.question.request.QuestionRequest;
import com.edu.espp.dto.question.response.QuestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface QuestionService {
    QuestionResponse getQuestionById(Long id);
    Page<QuestionResponse> getQuestionsByExam(Long examId, Pageable pageable);
    
    Page<QuestionResponse> getQuestionsBySkill(QuestionSkill skill, Pageable pageable);
    
    Page<QuestionResponse> getQuestionsByExamAndSkill(Long examId, QuestionSkill skill, Pageable pageable);

    QuestionResponse createQuestion(QuestionRequest request);
    QuestionResponse updateQuestion(Long id, QuestionRequest request);
    void deleteQuestion(Long id);
}
