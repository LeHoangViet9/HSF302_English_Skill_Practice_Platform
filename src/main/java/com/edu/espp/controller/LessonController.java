package com.edu.espp.controller;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.common.utils.PageableUtils;
import com.edu.espp.dto.lesson.request.LessonContentRequest;
import com.edu.espp.dto.lesson.request.LessonRequest;
import com.edu.espp.dto.lesson.response.LessonResponse;
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
        model.addAttribute(
                "lessonPage",
                lessonService.searchLessons(keyword, type, level, pageable).map(lessonService::toLessonResponse)
        );
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
        boolean isNewLesson = lesson.getId() == null;
        LessonResponse savedLesson = lessonService.toLessonResponse(lessonService.saveLesson(lesson));
        if (isNewLesson) {
            return "redirect:/manage/lessons/edit/" + savedLesson.getId();
        }
        return "redirect:/manage/lessons";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("lesson", lessonService.toLessonRequest(lessonService.getLessonById(id)));
        model.addAttribute("types", TypeLesson.values());
        model.addAttribute("levels", LevelLesson.values());
        model.addAttribute("contents", lessonService.toLessonContentResponses(lessonService.getContentsByLesson(id)));
        return "staff/lesson-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return "redirect:/manage/lessons";
    }

    @GetMapping("/{lessonId}/contents")
    public String listContents(@PathVariable Long lessonId, Model model) {
        model.addAttribute("lesson", lessonService.toLessonResponse(lessonService.getLessonById(lessonId)));
        model.addAttribute("contents", lessonService.toLessonContentResponses(lessonService.getContentsByLesson(lessonId)));
        return "staff/contents-list";
    }

    @GetMapping("/{lessonId}/contents/create")
    public String showCreateContentForm(@PathVariable Long lessonId, Model model) {
        LessonResponse lesson = lessonService.toLessonResponse(lessonService.getLessonById(lessonId));
        int nextOrder = lessonService.getContentsByLesson(lessonId).size() + 1;

        LessonContentRequest content = LessonContentRequest.builder()
                .contentOrder(nextOrder)
                .build();

        model.addAttribute("lesson", lesson);
        model.addAttribute("content", content);
        return "staff/contents-form";
    }

    @PostMapping("/{lessonId}/contents/save")
    public String saveContent(
            @PathVariable Long lessonId,
            @ModelAttribute LessonContentRequest content
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
        LessonResponse lesson = lessonService.toLessonResponse(lessonService.getLessonById(lessonId));
        LessonContentRequest content = lessonService.toLessonContentRequest(lessonService.getContentById(contentId));
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
