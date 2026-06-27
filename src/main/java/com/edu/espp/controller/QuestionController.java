package com.edu.espp.controller;

import com.edu.espp.common.dto.response.ApiResponse;
import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.dto.question.request.QuestionRequest;
import com.edu.espp.dto.question.response.QuestionResponse;
import com.edu.espp.service.question.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Validated
public class QuestionController {

    private final QuestionService questionService;

    // 1. Lấy chi tiết câu hỏi theo ID
    @GetMapping("/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestionById(@PathVariable Long questionId) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy câu hỏi theo id thành công",
                questionService.getQuestionById(questionId),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    // 2. Lấy toàn bộ danh sách câu hỏi của một đề thi cụ thể
    @GetMapping("/exam/{examId}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsByExam(@PathVariable Long examId) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy danh sách câu hỏi theo bài thi thành công",
                questionService.getQuestionsByExam(examId),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    // 3. Lấy câu hỏi luyện tập tự do theo Kỹ năng (Ví dụ: LISTENING, READING)
    @GetMapping("/skill/{skill}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsBySkill(@PathVariable QuestionSkill skill) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy danh sách câu hỏi theo kỹ năng thành công",
                questionService.getQuestionsBySkill(skill),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    // 4. Lấy câu hỏi lọc theo cả Đề thi lẫn Kỹ năng cụ thể trong đề đó
    @GetMapping("/exam/{examId}/skill/{skill}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsByExamAndSkill(
            @PathVariable Long examId,
            @PathVariable QuestionSkill skill) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy danh sách câu hỏi theo bài thi và kỹ năng thành công",
                questionService.getQuestionsByExamAndSkill(examId, skill),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    // 5. Thêm mới một câu hỏi đơn lẻ (Admin)
    @PostMapping
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(@Valid @RequestBody QuestionRequest request) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Tạo câu hỏi thành công",
                questionService.createQuestion(request),
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

    // 6. Import/Tạo nhanh hàng loạt câu hỏi cùng lúc (Admin)
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> createBulkQuestions( @RequestBody List< @Valid QuestionRequest> requests) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Tạo danh sách câu hỏi thành công",
                questionService.createBulkQuestions(requests),
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

    // 7. Cập nhật nội dung câu hỏi theo ID (Admin)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Cập nhật câu hỏi thành công",
                questionService.updateQuestion(id, request),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    // 8. Xóa câu hỏi khỏi hệ thống (Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Xóa câu hỏi thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}