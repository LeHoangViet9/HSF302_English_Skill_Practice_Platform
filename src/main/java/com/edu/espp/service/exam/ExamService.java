package com.edu.espp.service.exam;

import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.dto.exam.request.ExamRequest;
import com.edu.espp.dto.exam.request.ExamSubmitRequest;
import com.edu.espp.dto.exam.response.ExamResponse;
import com.edu.espp.dto.exam.response.ExamResultResponse;
import com.edu.espp.dto.question.response.QuestionResponse;

import java.util.List;

public interface ExamService {
    List<ExamResponse> getAllExams();
    List<QuestionResponse> getQuestionsByExam(Long examId);
    ExamResultResponse submitExam(Long examId, Long userId, ExamSubmitRequest request);
    List<QuestionResponse> getQuestionsByExamAndSkill(Long examId, QuestionSkill skill);

    ExamResponse createExam(ExamRequest request);
    ExamResponse getExamById(Long examId);
    ExamResponse updateExam(Long examId, ExamRequest request);
    void deleteExam(Long examId);
}
