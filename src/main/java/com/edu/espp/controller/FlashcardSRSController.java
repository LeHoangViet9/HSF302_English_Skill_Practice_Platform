package com.edu.espp.controller;

import com.edu.espp.common.enums.ReviewResult;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.entity.Lesson;
import com.edu.espp.entity.LessonContent;
import com.edu.espp.entity.SRSReview;
import com.edu.espp.entity.User;
import com.edu.espp.service.LessonService;
import com.edu.espp.service.SRSReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/student/flashcards")
@RequiredArgsConstructor
public class FlashcardSRSController {

    private final SRSReviewService srsReviewService;
    private final LessonService lessonService;

    @GetMapping("/review")
    public ModelAndView reviewPage(
            @RequestParam(required = false) Long contentId,
            @RequestParam(required = false) String returnUrl,
            @ModelAttribute("user") User user
    ) {
        Long userId = (user != null) ? user.getId() : 1L;

        List<SRSReview> reviews;
        if (contentId != null) {
            java.util.Optional<SRSReview> specReview = srsReviewService.getReviewByUserAndContent(userId, contentId);
            if (specReview.isPresent()) {
                reviews = List.of(specReview.get());
            } else {
                // Auto add to deck if not already there
                srsReviewService.addToDeck(userId, contentId);
                reviews = srsReviewService.getReviewByUserAndContent(userId, contentId)
                        .map(List::of)
                        .orElse(List.of());
            }
        } else {
            reviews = srsReviewService.getDueReviews(userId);
        }

        ModelAndView mv = new ModelAndView();
        mv.setViewName("student/flashcards/review");
        mv.addObject("reviews", reviews);
        mv.addObject("returnUrl", returnUrl);

        return mv;
    }

    @PostMapping("/{contentId}/review")
    public String reviewFlashcard(
            @PathVariable Long contentId,
            @RequestParam ReviewResult result,
            @RequestParam(required = false) String returnUrl,
            @ModelAttribute("user") User user
    ) {
        Long userId = (user != null) ? user.getId() : 1L;

        srsReviewService.reviewFlashcard(userId, contentId, result);

        if (returnUrl != null && !returnUrl.isBlank()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/student/flashcards/review";
    }

    @GetMapping("/manage")
    public ModelAndView managePage(
            @RequestParam(required = false) Long lessonId,
            @ModelAttribute("user") User user
    ) {
        Long userId = (user != null) ? user.getId() : 1L;

        List<Lesson> lessons = lessonService.searchPublishedLessons(null, TypeLesson.VOCABULARY, null);

        Long selectedLessonId = lessonId;
        if (selectedLessonId == null && !lessons.isEmpty()) {
            selectedLessonId = lessons.get(0).getId();
        }

        List<LessonContent> contents = List.of();
        if (selectedLessonId != null) {
            contents = lessonService.getContentsByLesson(selectedLessonId);
        }

        Set<Long> deckContentIds = srsReviewService.getDeckContentIds(userId);

        ModelAndView mv = new ModelAndView();
        mv.setViewName("student/flashcards/manage");
        mv.addObject("lessons", lessons);
        mv.addObject("selectedLessonId", selectedLessonId);
        mv.addObject("contents", contents);
        mv.addObject("deckContentIds", deckContentIds);
        return mv;
    }

    @PostMapping("/manage/toggle/{contentId}")
    public String toggleFlashcard(
            @PathVariable Long contentId,
            @RequestParam(required = false) String returnUrl,
            @ModelAttribute("user") User user
    ) {
        Long userId = (user != null) ? user.getId() : 1L;

        srsReviewService.toggleDeck(userId, contentId);

        if (returnUrl != null && !returnUrl.isBlank()) {
            return "redirect:" + returnUrl;
        }
        LessonContent content = lessonService.getContentById(contentId);
        return "redirect:/student/flashcards/manage?lessonId=" + content.getLesson().getId();
    }
}