package com.edu.espp.controller;

import com.edu.espp.common.dto.response.ApiResponse;
import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.dto.exam.request.ExamRequest;
import com.edu.espp.dto.exam.request.ExamSubmitRequest;
import com.edu.espp.dto.exam.response.ExamResponse;
import com.edu.espp.dto.exam.response.ExamResultResponse;
import com.edu.espp.dto.question.response.QuestionResponse;
import com.edu.espp.service.exam.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getAllExams() {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy danh sách bài thi thành công",
                examService.getAllExams(),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/{examId}/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getAllQuestionInExam(@PathVariable Long examId) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy danh sách câu hỏi thành công",
                examService.getQuestionsByExam(examId),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/{examId}/skills")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsByExamAndSkill(
            @PathVariable Long examId,
            @RequestParam QuestionSkill skill) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy danh sách câu hỏi theo kỹ năng thành công",
                examService.getQuestionsByExamAndSkill(examId, skill),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PostMapping("/{examId}/submit")
    public ResponseEntity<ApiResponse<ExamResultResponse>> submitExam(
            @PathVariable Long examId,
            @RequestParam Long userId,
            @Valid @RequestBody ExamSubmitRequest examSubmitRequest) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Nộp bài và chấm điểm thành công",
                examService.submitExam(examId, userId, examSubmitRequest),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(@Valid @RequestBody ExamRequest examRequest) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Tạo đề thi mới thành công",
                examService.createExam(examRequest),
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

    @GetMapping("/{examId}")
    public ResponseEntity<ApiResponse<ExamResponse>> getExamById(@PathVariable Long examId) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy thông tin đề thi chi tiết thành công",
                examService.getExamById(examId),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PutMapping("/{examId}")
    public ResponseEntity<ApiResponse<ExamResponse>> updateExam(
            @PathVariable Long examId,
            @Valid @RequestBody ExamRequest examRequest) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Cập nhật thông tin đề thi thành công",
                examService.updateExam(examId, examRequest),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @DeleteMapping("/{examId}")
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable Long examId) {
        examService.deleteExam(examId);
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Xóa đề thi thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}