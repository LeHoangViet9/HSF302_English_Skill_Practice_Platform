package com.edu.espp.controller.student;

import com.edu.espp.dto.exam.response.ExamHistoryResponse;
import com.edu.espp.service.history.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/histories")
public class ExamHistoryController {

    private final ExamHistoryService examHistoryService;

    // Xem lịch sử thi của một User cụ thể dưới dạng trang web công khai
    @GetMapping("/user/{userId}")
    public String getUserHistory(@PathVariable Long userId, Model model) {
        List<ExamHistoryResponse> histories = examHistoryService.getUserExamHistory(userId);
        model.addAttribute("histories", histories);
        return "student/history/user-history";
    }
}