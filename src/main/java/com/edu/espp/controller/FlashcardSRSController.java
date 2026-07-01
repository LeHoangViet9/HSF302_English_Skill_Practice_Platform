package com.edu.espp.controller;

import com.edu.espp.common.enums.ReviewResult;
import com.edu.espp.entity.SRSReview;
import com.edu.espp.service.SRSReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/student/flashcards")
@RequiredArgsConstructor
public class FlashcardSRSController {

    private final SRSReviewService srsReviewService;

    @GetMapping("/review")
    public ModelAndView reviewPage() {
        Long userId = 1L; // tạm thời hard-code, sau này lấy từ user đăng nhập

        List<SRSReview> reviews = srsReviewService.getDueReviews(userId);

        ModelAndView mv = new ModelAndView();
        mv.setViewName("student/flashcards/review");
        mv.addObject("reviews", reviews);

        return mv;
    }

    @PostMapping("/{contentId}/review")
    public String reviewFlashcard(
            @PathVariable Long contentId,
            @RequestParam ReviewResult result
    ) {
        Long userId = 1L;

        srsReviewService.reviewFlashcard(userId, contentId, result);

        return "redirect:/student/flashcards/review";
    }
}