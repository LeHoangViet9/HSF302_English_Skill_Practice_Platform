package com.edu.espp.controller;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.common.utils.PageableUtils;
import com.edu.espp.entity.Lesson;
import com.edu.espp.service.LessonService;
import com.edu.espp.entity.LessonContent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manage/lessons")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping
    public String listLessons(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TypeLesson type,
            @RequestParam(required = false) LevelLesson level,
            Model model
    ) {
        Pageable pageable = PageableUtils.generate(page, size, "id", "desc");
        model.addAttribute("lessonPage", lessonService.searchLessons(keyword, type, level, pageable));

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
        return "redirect:/manage/lessons";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Lesson lesson = lessonService.getLessonById(id);

        model.addAttribute("lesson", lesson);
        model.addAttribute("types", TypeLesson.values());
        model.addAttribute("levels", LevelLesson.values());
        model.addAttribute("contents", lessonService.getContentsByLesson(id));

        return "staff/lesson-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return "redirect:/manage/lessons";
    }

    @GetMapping("/{lessonId}/contents")
    public String listContents(@PathVariable Long lessonId) {
        return "redirect:/manage/lessons/edit/" + lessonId;
    }

    @GetMapping("/{lessonId}/contents/create")
    public String showCreateContentForm(@PathVariable Long lessonId, Model model) {
        Lesson lesson = lessonService.getLessonById(lessonId);
        int nextOrder = lessonService.getContentsByLesson(lessonId).size() + 1;

        LessonContent content = LessonContent.builder()
                .lesson(lesson)
                .contentOrder(nextOrder)
                .build();

        model.addAttribute("lesson", lesson);
        model.addAttribute("content", content);
        return "staff/contents-form";
    }

    @PostMapping("/{lessonId}/contents/save")
    public String saveContent(
            @PathVariable Long lessonId,
            @ModelAttribute LessonContent content
    ) {
        lessonService.saveLessonContent(lessonId, content);
        return "redirect:/manage/lessons/edit/" + lessonId;
    }

    @GetMapping("/{lessonId}/contents/edit/{contentId}")
    public String showEditContentForm(
            @PathVariable Long lessonId,
            @PathVariable Long contentId,
            Model model
    ) {
        Lesson lesson = lessonService.getLessonById(lessonId);
        LessonContent content = lessonService.getContentById(contentId);

        model.addAttribute("lesson", lesson);
        model.addAttribute("content", content);
        return "staff/contents-form";
    }

    @GetMapping("/{lessonId}/contents/delete/{contentId}")
    public String deleteContent(
            @PathVariable Long lessonId,
            @PathVariable Long contentId
    ) {
        lessonService.deleteLessonContent(contentId);
        return "redirect:/manage/lessons/edit/" + lessonId;
    }
}
