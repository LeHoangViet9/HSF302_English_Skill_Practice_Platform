package com.edu.espp.service.question;

import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.dto.question.request.QuestionRequest;
import com.edu.espp.dto.question.response.QuestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface QuestionService {
    QuestionResponse getQuestionById(Long id);

    /**
     * Lọc câu hỏi động: truyền null vào tham số nào thì bỏ qua điều kiện đó.
     * Thay thế cho 3 method: getQuestionsByExam, getQuestionsBySkill, getQuestionsByExamAndSkill
     *
     * @param examId  null = không lọc theo đề thi
     * @param skill   null = không lọc theo kỹ năng
     */
    Page<QuestionResponse> getQuestions(Long examId, QuestionSkill skill, Pageable pageable);

    QuestionResponse createQuestion(QuestionRequest request);
    QuestionResponse updateQuestion(Long id, QuestionRequest request);
    void deleteQuestion(Long id);
}
