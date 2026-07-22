package com.edu.espp.controller;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.dto.lesson.request.LessonRequest;
import com.edu.espp.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff/lessons")
public class StaffLessonController {

    private final LessonService lessonService;

    @GetMapping
    public String listLessons(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TypeLesson type,
            @RequestParam(required = false) LevelLesson level,
            Model model
    ) {
        model.addAttribute(
                "lessons",
                lessonService.searchLessons(keyword, type, level, Pageable.unpaged()).map(lessonService::toLessonResponse));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("types", TypeLesson.values());
        model.addAttribute("levels", LevelLesson.values());
        return "staff/lesson-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("lesson", new LessonRequest());
        model.addAttribute("types", TypeLesson.values());
        model.addAttribute("levels", LevelLesson.values());
        return "staff/lesson-form";
    }

    @PostMapping("/save")
    public String saveLesson(@ModelAttribute LessonRequest lesson) {
        lessonService.saveLesson(lesson);
        return "redirect:/staff/lessons";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("lesson", lessonService.toLessonRequest(lessonService.getLessonById(id)));
        model.addAttribute("types", TypeLesson.values());
        model.addAttribute("levels", LevelLesson.values());
        return "staff/lesson-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return "redirect:/staff/lessons";
    }
}
