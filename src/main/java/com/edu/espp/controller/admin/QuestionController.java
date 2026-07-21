package com.edu.espp.controller.admin;

import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.common.utils.PageableUtils;
import com.edu.espp.dto.question.request.QuestionRequest;
import com.edu.espp.dto.question.response.QuestionResponse;
import com.edu.espp.service.question.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/manage/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    // 1. Quản lý danh sách câu hỏi tổng quan hoặc theo đề thi (Admin)
    @GetMapping
    public String listQuestions(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) QuestionSkill skill,
            Model model) {

        Pageable pageable = PageableUtils.generate(page, size, "id", "desc");
        Page<QuestionResponse> questionPage;
        
        if (examId != null && skill != null) {
            questionPage = questionService.getQuestionsByExamAndSkill(examId, skill, pageable);
        } else if (examId != null) {
            questionPage = questionService.getQuestionsByExam(examId, pageable);
        } else if (skill != null) {
            questionPage = questionService.getQuestionsBySkill(skill, pageable);
        } else {
            questionPage = questionService.getQuestionsBySkill(QuestionSkill.READING, pageable);
        }

        model.addAttribute("questionPage", questionPage);
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
    public String showCreateForm(@RequestParam(required = false) Long examId, Model model) {
        QuestionRequest request = new QuestionRequest();
        if (examId != null) {
            request.setExamId(examId);
        }
        model.addAttribute("questionRequest", request);
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
        return "redirect:/manage/questions";
    }

    // 5. Màn hình sửa câu hỏi (Admin)
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        QuestionResponse question = questionService.getQuestionById(id);
        model.addAttribute("question", question);
        
        String optionsJson = "";
        try {
            if (question.getOptions() != null) {
                optionsJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(question.getOptions());
            }
        } catch (Exception ignored) {}
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
        return "redirect:/manage/questions";
    }

    // 7. Xóa câu hỏi khỏi hệ thống
    @PostMapping("/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        questionService.deleteQuestion(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa câu hỏi thành công!");
        return "redirect:/manage/questions";
    }
}
