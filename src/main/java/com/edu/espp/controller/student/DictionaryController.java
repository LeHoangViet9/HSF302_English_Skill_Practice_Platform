package com.edu.espp.controller.student;

import com.edu.espp.dto.bookmark.response.BookmarkResponse;
import com.edu.espp.dto.lesson.response.LessonContentResponse;
import com.edu.espp.dto.lesson.response.LessonResponse;
import com.edu.espp.entity.User;
import com.edu.espp.service.DictionaryService;
import com.edu.espp.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;
    private final LessonService lessonService;

    @GetMapping("/student/dictionary")
    public String dictionary(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long lessonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @ModelAttribute("user") User user,
            Model model
    ) {
        Page<LessonContentResponse> dictionaryPage = dictionaryService.search(
                keyword,
                lessonId,
                PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by("id").ascending())
        ).map(lessonService::toLessonContentResponse);
        List<LessonResponse> lessons = lessonService.toLessonResponses(
                lessonService.searchPublishedLessons(null, null, null)
        );

        model.addAttribute("results", dictionaryPage.getContent());
        model.addAttribute("dictionaryPage", dictionaryPage);
        model.addAttribute("page", dictionaryPage.getNumber());
        model.addAttribute("size", dictionaryPage.getSize());
        model.addAttribute("lessons", lessons);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedLessonId", lessonId);
        model.addAttribute("bookmarkedContentIds", dictionaryService.getBookmarkedContentIds(user.getId()));

        return "student/dictionary/index";
    }

    @GetMapping("/student/bookmarks")
    public String bookmarks(@ModelAttribute("user") User user, Model model) {
        List<BookmarkResponse> bookmarks = dictionaryService.getBookmarkResponses(user.getId());
        model.addAttribute("bookmarks", bookmarks);
        return "student/dictionary/bookmarks";
    }

    @PostMapping("/student/dictionary/bookmark/{contentId}")
    public String addBookmark(
            @PathVariable Long contentId,
            @RequestParam(required = false) String returnUrl,
            @ModelAttribute("user") User user
    ) {
        dictionaryService.addBookmark(user.getId(), contentId);
        return redirectTo(returnUrl, "/student/dictionary");
    }

    @PostMapping("/student/dictionary/bookmark/{contentId}/delete")
    public String removeBookmark(
            @PathVariable Long contentId,
            @RequestParam(required = false) String returnUrl,
            @ModelAttribute("user") User user
    ) {
        dictionaryService.removeBookmark(user.getId(), contentId);
        return redirectTo(returnUrl, "/student/dictionary");
    }

    @GetMapping("/student/dictionary/api")
    @ResponseBody
    public List<LessonContentResponse> apiDictionary(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long lessonId
    ) {
        return dictionaryService.search(keyword, lessonId).stream()
                .map(lessonService::toLessonContentResponse)
                .toList();
    }

    private String redirectTo(String returnUrl, String fallback) {
        if (returnUrl == null || returnUrl.isBlank() || !returnUrl.startsWith("/")) {
            return "redirect:" + fallback;
        }
        return "redirect:" + returnUrl;
    }
}
