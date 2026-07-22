package com.edu.espp.controller.student;

import com.edu.espp.dto.exam.request.ExamSubmitRequest;
import com.edu.espp.dto.exam.response.ExamResponse;
import com.edu.espp.dto.exam.response.ExamResultResponse;
import com.edu.espp.service.exam.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller("studentExamController")
@RequiredArgsConstructor
@RequestMapping("/student/exams")
public class ExamController {
    private final ExamService examService;

    // Xem danh sách đề thi (Redirect sang trang chung)
    @GetMapping
    public String getAllExams() {
        return "redirect:/exams";
    }

    // Vào làm bài thi (lấy danh sách câu hỏi kèm theo đề)
    @GetMapping("/{examId}/take")
    public String takeExam(@PathVariable Long examId, Model model) {
        model.addAttribute("exam", examService.getApprovedExamById(examId));
        model.addAttribute("questions", examService.getQuestionsByExam(examId));
        model.addAttribute("submitForm", new ExamSubmitRequest()); // Form để hứng data nộp bài
        return "student/exam/take-exam"; // Trả về templates/student/exam/take-exam.html
    }

    // Nộp bài thi và chấm điểm
    @PostMapping("/{examId}/submit")
    public String submitExam(
            @PathVariable Long examId,
            @RequestParam Long userId,
            @ModelAttribute("submitForm") ExamSubmitRequest examSubmitRequest,
            Model model) {

        ExamResultResponse result = examService.submitExam(examId, userId, examSubmitRequest);
        model.addAttribute("result", result);
        return "student/exam/result"; // Trả về màn hình templates/student/exam/result.html hiển thị điểm
    }
}
