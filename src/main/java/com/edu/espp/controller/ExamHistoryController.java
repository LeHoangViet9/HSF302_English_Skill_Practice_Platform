package com.edu.espp.controller;

import com.edu.espp.common.dto.response.ApiResponse;
import com.edu.espp.dto.exam.response.ExamHistoryResponse;
import com.edu.espp.service.history.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/histories")
public class ExamHistoryController {
    private final ExamHistoryService examHistoryService;

    // API lấy lịch sử thi của một User cụ thể
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ExamHistoryResponse>>> getUserHistory(@PathVariable Long userId) {
        List<ExamHistoryResponse> response = examHistoryService.getUserExamHistory(userId);
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy lịch sử làm bài thành công",
                response,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}
