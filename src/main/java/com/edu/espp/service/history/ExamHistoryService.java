package com.edu.espp.service.history;

import com.edu.espp.dto.exam.response.ExamHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExamHistoryService {
    List<ExamHistoryResponse> getUserExamHistory(Long userId);
    Page<ExamHistoryResponse> getUserExamHistory(Long userId, Pageable pageable);
    
    Page<ExamHistoryResponse> getAllExamHistories(Pageable pageable);
    
}
