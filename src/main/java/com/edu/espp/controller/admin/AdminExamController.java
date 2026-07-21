package com.edu.espp.controller.admin;

import com.edu.espp.dto.exam.request.ExamRequest;
import com.edu.espp.service.exam.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/manage/exams")
@RequiredArgsConstructor
public class AdminExamController {

    private final ExamService examService;

    // Xem danh sách đề thi (Redirect sang trang chung)
    @GetMapping
    public String getAllExams() {
        return "redirect:/exams";
    }

    // Màn hình thêm đề thi mới (Admin)
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("examRequest", new ExamRequest());
        return "admin/exam/create"; // Trả về templates/admin/exam/create.html
    }

    // Xử lý thêm đề thi mới (Admin)
    @PostMapping
    public String createExam(@Valid @ModelAttribute ExamRequest examRequest, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/exam/create";
        }
        examService.createExam(examRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo đề thi mới thành công!");
        return "redirect:/manage/exams";
    }

    // Màn hình cập nhật thông tin đề thi (Admin)
    @GetMapping("/{examId}/edit")
    public String showEditForm(@PathVariable Long examId, Model model) {
        model.addAttribute("exam", examService.getExamById(examId));
        return "admin/exam/edit";
    }

    // Xử lý cập nhật thông tin đề thi
    @PostMapping("/{examId}/update")
    public String updateExam(
            @PathVariable Long examId,
            @Valid @ModelAttribute ExamRequest examRequest,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/exam/edit";
        }
        examService.updateExam(examId, examRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đề thi thành công!");
        return "redirect:/manage/exams";
    }

    // Xử lý xóa đề thi
    @PostMapping("/{examId}/delete")
    public String deleteExam(@PathVariable Long examId, RedirectAttributes redirectAttributes) {
        examService.deleteExam(examId);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa đề thi thành công!");
        return "redirect:/manage/exams";
    }
}