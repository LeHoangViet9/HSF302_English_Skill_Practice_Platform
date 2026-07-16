package com.edu.espp.controller;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.entity.Lesson;
import com.edu.espp.entity.LessonContent;
import com.edu.espp.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("lessons", lessonService.searchLessons(keyword, type, level));

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedLevel", level);

        model.addAttribute("types", TypeLesson.values());
        model.addAttribute("levels", LevelLesson.values());

        return "staff/lesson-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("lesson", new Lesson());
        model.addAttribute("types", TypeLesson.values());
        model.addAttribute("levels", LevelLesson.values());

        return "staff/lesson-form";
    }

    @PostMapping("/save")
    public String saveLesson(@ModelAttribute Lesson lesson) {
        lessonService.saveLesson(lesson);

        return "redirect:/staff/lessons";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Lesson lesson = lessonService.getLessonById(id);

        model.addAttribute("lesson", lesson);
        model.addAttribute("contents", lessonService.getContentsByLesson(id));
        model.addAttribute("lessonContent", new LessonContent());
        model.addAttribute("types", TypeLesson.values());
        model.addAttribute("levels", LevelLesson.values());

        return "staff/lesson-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);

        return "redirect:/staff/lessons";
    }

    @PostMapping("/{lessonId}/contents/save")
    public String saveLessonContent(
            @PathVariable Long lessonId,
            @ModelAttribute LessonContent lessonContent
    ) {
        lessonService.saveLessonContent(lessonId, lessonContent);
        return "redirect:/staff/lessons/edit/" + lessonId;
    }

    @GetMapping("/{lessonId}/contents/edit/{contentId}")
    public String editLessonContent(
            @PathVariable Long lessonId,
            @PathVariable Long contentId,
            Model model
    ) {
        Lesson lesson = lessonService.getLessonById(lessonId);
        LessonContent content = lessonService.getContentById(contentId);

        model.addAttribute("lesson", lesson);
        model.addAttribute("contents", lessonService.getContentsByLesson(lessonId));
        model.addAttribute("lessonContent", content);
        model.addAttribute("types", TypeLesson.values());
        model.addAttribute("levels", LevelLesson.values());

        return "staff/lesson-form";
    }

    @GetMapping("/{lessonId}/contents/delete/{contentId}")
    public String deleteLessonContent(
            @PathVariable Long lessonId,
            @PathVariable Long contentId
    ) {
        lessonService.deleteLessonContent(contentId);
        return "redirect:/staff/lessons/edit/" + lessonId;
    }
}
