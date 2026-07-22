package com.edu.espp.controller.student;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.common.utils.PageableUtils;
import com.edu.espp.dto.lesson.response.LessonResponse;
import com.edu.espp.entity.User;
import com.edu.espp.service.LearningContentService;
import com.edu.espp.service.LessonService;
import com.edu.espp.service.SRSReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/lessons")
public class LearningContentController {

    private final LessonService lessonService;
    private final LearningContentService learningContentService;
    private final SRSReviewService srsReviewService;

    @GetMapping
    public String listLessons(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TypeLesson type,
            @RequestParam(required = false) LevelLesson level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @ModelAttribute("user") User user,
            Model model
    ) {
        Page<LessonResponse> lessonPage = lessonService.searchPublishedLessons(
                keyword,
                type,
                level,
                PageableUtils.generate(page, size, "id", "asc")
        ).map(lessonService::toLessonResponse);

        model.addAttribute("lessons", lessonPage.getContent());
        model.addAttribute("lessonPage", lessonPage);
        model.addAttribute("page", lessonPage.getNumber());
        model.addAttribute("size", lessonPage.getSize());
        model.addAttribute("completedLessonIds", learningContentService.getCompletedLessonIds(user.getId()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("types", TypeLesson.values());
        model.addAttribute("levels", LevelLesson.values());
        return "student/lessons/list";
    }

    @GetMapping("/{id}")
    public String lessonDetail(
            @PathVariable Long id,
            @ModelAttribute("user") User user,
            Model model
    ) {
        model.addAttribute("lesson", lessonService.toLessonResponse(lessonService.getApprovedLessonById(id)));
        model.addAttribute("contents", lessonService.toLessonContentResponses(lessonService.getContentsByLesson(id)));
        model.addAttribute("completed", learningContentService.isCompleted(user.getId(), id));
        model.addAttribute("deckContentIds", srsReviewService.getDeckContentIds(user.getId()));
        return "student/lessons/detail";
    }

    @RequestMapping(value = "/{id}/complete", method = {RequestMethod.GET, RequestMethod.POST})
    public String markCompleted(@PathVariable Long id, @ModelAttribute("user") User user) {
        learningContentService.markCompleted(user.getId(), id);
        return "redirect:/student/lessons/" + id;
    }

    @GetMapping("/api")
    @ResponseBody
    public List<LessonResponse> apiLessons(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TypeLesson type,
            @RequestParam(required = false) LevelLesson level
    ) {
        return lessonService.searchPublishedLessons(keyword, type, level).stream()
                .map(lessonService::toLessonResponse)
                .toList();
    }
}
