package com.edu.espp.controller.admin;

import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.dto.question.request.QuestionRequest;
import com.edu.espp.dto.question.response.QuestionResponse;
import com.edu.espp.service.question.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    // 1. Quản lý danh sách câu hỏi tổng quan hoặc theo đề thi (Admin)
    @GetMapping
    public String listQuestions(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) QuestionSkill skill,
            Model model) {

        List<QuestionResponse> questions;
        if (examId != null && skill != null) {
            questions = questionService.getQuestionsByExamAndSkill(examId, skill);
        } else if (examId != null) {
            questions = questionService.getQuestionsByExam(examId);
        } else if (skill != null) {
            questions = questionService.getQuestionsBySkill(skill);
        } else {
            questions = questionService.getQuestionsBySkill(QuestionSkill.READING);
        }

        model.addAttribute("questions", questions);
        return "admin/question/list"; // Trả về templates/admin/question/list.html
    }

    // 2. Xem chi tiết câu hỏi
    @GetMapping("/{questionId}")
    public String getQuestionById(@PathVariable Long questionId, Model model) {
        model.addAttribute("question", questionService.getQuestionById(questionId));
        return "admin/question/detail";
    }

    // 3. Màn hình thêm mới câu hỏi (Admin)
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("questionRequest", new QuestionRequest());
        return "admin/question/create";
    }

    // 4. Xử lý lưu câu hỏi mới
    @PostMapping
    public String createQuestion(
            @Valid @ModelAttribute QuestionRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/question/create";
        }
        questionService.createQuestion(request);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo câu hỏi thành công!");
        return "redirect:/admin/questions";
    }

    // 5. Màn hình sửa câu hỏi (Admin)
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        com.edu.espp.dto.question.response.QuestionResponse question = questionService.getQuestionById(id);
        model.addAttribute("question", question);
        
        String optionsJson = "";
        try {
            if (question.getOptions() != null) {
                optionsJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(question.getOptions());
            }
        } catch (Exception e) {}
        model.addAttribute("optionsJson", optionsJson);
        
        return "admin/question/edit";
    }

    // 6. Xử lý lưu cập nhật câu hỏi
    @PostMapping("/{id}/update")
    public String updateQuestion(
            @PathVariable Long id,
            @Valid @ModelAttribute QuestionRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/question/edit";
        }
        questionService.updateQuestion(id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật câu hỏi thành công!");
        return "redirect:/admin/questions";
    }

    // 7. Xóa câu hỏi khỏi hệ thống
    @PostMapping("/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        questionService.deleteQuestion(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa câu hỏi thành công!");
        return "redirect:/admin/questions";
    }
}
