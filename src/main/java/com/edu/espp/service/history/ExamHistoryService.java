package com.edu.espp.service.history;

import com.edu.espp.dto.exam.response.ExamAttemptDetailResponse;
import com.edu.espp.dto.exam.response.ExamHistoryResponse;

import java.util.List;

public interface ExamHistoryService {
    List<ExamHistoryResponse> getUserExamHistory(Long userId);
    List<ExamHistoryResponse> getAllExamHistories();
    List<ExamAttemptDetailResponse> getExamAttemptDetails(Long examHistoryId);
}
