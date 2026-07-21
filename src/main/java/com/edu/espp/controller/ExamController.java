package com.edu.espp.controller;

import com.edu.espp.common.enums.Role;
import com.edu.espp.dto.exam.response.ExamResponse;
import com.edu.espp.entity.User;
import com.edu.espp.service.exam.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import com.edu.espp.common.enums.TypeQuiz;

@Controller("commonExamController")
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public String getAllExams(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) TypeQuiz type,
            @ModelAttribute("user") User user,
            Model model) {

        // Determine if we should include all statuses (admin & staff see all, student only sees APPROVED)
        boolean includeAllStatuses = user != null &&
                (user.getRole() == Role.ADMIN || user.getRole() == Role.STAFF);

        // Lấy danh sách đề thi từ Service
        List<ExamResponse> exams = examService.searchExams(keyword, type, includeAllStatuses);

        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("keyword", keyword.trim());
        }
        if (type != null) {
            model.addAttribute("type", type);
        }

        model.addAttribute("exams", exams);

        // Trả về templates/exam/list.html
        return "exam/list";
    }
}
