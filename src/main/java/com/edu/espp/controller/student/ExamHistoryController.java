package com.edu.espp.controller.student;

import com.edu.espp.common.utils.PageableUtils;
import com.edu.espp.service.history.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
@RequestMapping("/histories")
public class ExamHistoryController {

    private final ExamHistoryService examHistoryService;

    // Xem lịch sử thi của một User cụ thể dưới dạng trang web công khai
    @GetMapping("/user/{userId}")
    public String getUserHistory(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageableUtils.generate(page, size, "testedAt", "desc");
        model.addAttribute("historyPage", examHistoryService.getUserExamHistory(userId, pageable));
        model.addAttribute("userId", userId);
        return "student/history/user-history";
    }
}