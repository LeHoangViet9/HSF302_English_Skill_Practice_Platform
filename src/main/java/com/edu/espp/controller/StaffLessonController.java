package com.edu.espp.controller;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.entity.Lesson;
import com.edu.espp.entity.LessonContent;
import com.edu.espp.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        Page<Lesson> lessonPage = lessonService.searchLessons(
                keyword,
                type,
                level,
                PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by("id").ascending())
        );

        model.addAttribute("lessons", lessonPage.getContent());
        model.addAttribute("lessonPage", lessonPage);
        model.addAttribute("page", lessonPage.getNumber());
        model.addAttribute("size", lessonPage.getSize());

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
    public String saveLesson(@Valid @ModelAttribute("lesson") Lesson lesson, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            addLessonFormAttributes(model);
            if (lesson.getId() != null) {
                model.addAttribute("contents", lessonService.getContentsByLesson(lesson.getId()));
                model.addAttribute("lessonContent", new LessonContent());
            }
            return "staff/lesson-form";
        }

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
            @Valid @ModelAttribute("lessonContent") LessonContent lessonContent,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            Lesson lesson = lessonService.getLessonById(lessonId);
            model.addAttribute("lesson", lesson);
            model.addAttribute("contents", lessonService.getContentsByLesson(lessonId));
            addLessonFormAttributes(model);
            return "staff/lesson-form";
        }

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

    private void addLessonFormAttributes(Model model) {
        model.addAttribute("types", TypeLesson.values());
        model.addAttribute("levels", LevelLesson.values());
    }
}
