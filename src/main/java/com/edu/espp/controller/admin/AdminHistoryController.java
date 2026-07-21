package com.edu.espp.controller.admin;
import com.edu.espp.service.history.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/histories")
public class AdminHistoryController {
    private final ExamHistoryService examHistoryService;

    // Admin xem danh sách lịch sử thi của TẤT CẢ các học viên trong hệ thống
    @GetMapping
    public String getAllHistories(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            Model model) {
        org.springframework.data.domain.Pageable pageable = com.edu.espp.common.utils.PageableUtils.generate(page, size, "testedAt", "desc");
        model.addAttribute("historyPage", examHistoryService.getAllExamHistories(pageable));
        return "admin/history/list";
    }

    // Admin tra cứu chi tiết lịch sử thi của một học viên cụ thể theo ID
    @GetMapping("/user/{userId}")
    public String getUserHistoryForAdmin(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            Model model) {
        org.springframework.data.domain.Pageable pageable = com.edu.espp.common.utils.PageableUtils.generate(page, size, "testedAt", "desc");
        model.addAttribute("historyPage", examHistoryService.getUserExamHistory(userId, pageable));
        model.addAttribute("userId", userId);
        return "admin/history/user-detail";
    }
}
