package com.edu.espp.controller.admin;

import com.edu.espp.common.utils.PageableUtils;
import com.edu.espp.service.LessonService;
import com.edu.espp.service.exam.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/approvals")
@RequiredArgsConstructor
public class AdminApprovalController {

    private final LessonService lessonService;
    private final ExamService examService;

    @GetMapping
    public String listPendingApprovals(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            Model model) {

        // Use "id" instead of "createdAt" because createdAt might not be mapped in all
        // projections, or "id" is safer.
        Pageable pageable = PageableUtils.generate(page, size, "id", "desc");

        model.addAttribute("lessonPage",
                lessonService.getPendingLessons(pageable).map(lessonService::toLessonResponse));

        model.addAttribute("examPage", examService.getPendingExams(pageable));

        return "admin/approval/list";
    }

    @PostMapping("/lessons/{id}/approve")
    public String approveLesson(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        lessonService.approveLesson(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt bài học thành công!");
        return "redirect:/admin/approvals";
    }

    @PostMapping("/lessons/{id}/reject")
    public String rejectLesson(@PathVariable Long id, @RequestParam String reason,
            RedirectAttributes redirectAttributes) {
        lessonService.rejectLesson(id, reason);
        redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối bài học.");
        return "redirect:/admin/approvals";
    }

    @PostMapping("/exams/{id}/approve")
    public String approveExam(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        examService.approveExam(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt bài thi thành công!");
        return "redirect:/admin/approvals";
    }

    @PostMapping("/exams/{id}/reject")
    public String rejectExam(@PathVariable Long id, @RequestParam String reason,
            RedirectAttributes redirectAttributes) {
        examService.rejectExam(id, reason);
        redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối bài thi.");
        return "redirect:/admin/approvals";
    }
}
