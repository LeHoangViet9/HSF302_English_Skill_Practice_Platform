package com.edu.espp.service.history;

import com.edu.espp.dto.exam.response.ExamHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExamHistoryService {
    Page<ExamHistoryResponse> getUserExamHistory(Long userId, Pageable pageable);
    
    // Thêm method trả về List không phân trang cho ProfileService
    List<ExamHistoryResponse> getUserExamHistory(Long userId);
    Page<ExamHistoryResponse> getAllExamHistories(Pageable pageable);
    
}
