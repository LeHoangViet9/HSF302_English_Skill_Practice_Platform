package com.edu.espp.controller.admin;
import com.edu.espp.dto.exam.response.ExamHistoryResponse;
import com.edu.espp.service.history.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/histories")
public class AdminHistoryController {
    private final ExamHistoryService examHistoryService;

    // Admin xem danh sách lịch sử thi của TẤT CẢ các học viên trong hệ thống
    @GetMapping
    public String getAllHistories(Model model) {
        // Bạn có thể bổ sung hàm getAllExamHistories() trong Service nếu muốn
        // model.addAttribute("histories", examHistoryService.getAllExamHistories());
        return "admin/history/list";
    }

    // Admin tra cứu chi tiết lịch sử thi của một học viên cụ thể theo ID
    @GetMapping("/user/{userId}")
    public String getUserHistoryForAdmin(@PathVariable Long userId, Model model) {
        List<ExamHistoryResponse> histories = examHistoryService.getUserExamHistory(userId);
        model.addAttribute("histories", histories);
        return "admin/history/user-detail";
    }
}
