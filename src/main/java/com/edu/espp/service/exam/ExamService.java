package com.edu.espp.service.exam;

import com.edu.espp.dto.exam.request.ExamRequest;
import com.edu.espp.dto.exam.request.ExamSubmitRequest;
import com.edu.espp.dto.exam.response.ExamResponse;
import com.edu.espp.dto.exam.response.ExamResultResponse;
import com.edu.espp.dto.question.response.QuestionResponse;

import java.util.List;

import com.edu.espp.common.enums.TypeQuiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExamService {
    List<ExamResponse> searchExams(String keyword, TypeQuiz type, boolean includeAllStatuses);
    List<QuestionResponse> getQuestionsByExam(Long examId);
    ExamResultResponse submitExam(Long examId, Long userId, ExamSubmitRequest request);

    ExamResponse createExam(ExamRequest request);
    ExamResponse getExamById(Long examId);
    ExamResponse updateExam(Long examId, ExamRequest request);
    void deleteExam(Long examId);

    Page<ExamResponse> getPendingExams(Pageable pageable);
    void approveExam(Long examId);
    void rejectExam(Long examId, String reason);
}
