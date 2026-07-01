package com.edu.espp.service.question;

import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.dto.question.request.QuestionRequest;
import com.edu.espp.dto.question.response.QuestionResponse;

import java.util.List;

public interface QuestionService {
    // Lấy câu hỏi
    QuestionResponse getQuestionById(Long id);
    List<QuestionResponse> getQuestionsByExam(Long examId);
    List<QuestionResponse> getQuestionsBySkill(QuestionSkill skill);
    List<QuestionResponse> getQuestionsByExamAndSkill(Long examId, QuestionSkill skill);

    // Quản lý CRUD (Admin)
    QuestionResponse createQuestion(QuestionRequest request);
    List<QuestionResponse> createBulkQuestions(List<QuestionRequest> requests);
    QuestionResponse updateQuestion(Long id, QuestionRequest request);
    void deleteQuestion(Long id);
}
