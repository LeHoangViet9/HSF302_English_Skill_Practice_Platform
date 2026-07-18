package com.edu.espp.service;

import com.edu.espp.entity.BookMark;
import com.edu.espp.entity.LessonContent;
import com.edu.espp.entity.User;
import com.edu.espp.repository.BookMarkRepository;
import com.edu.espp.repository.LessonContentRepository;
import com.edu.espp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictionaryService {

    private final LessonContentRepository lessonContentRepository;
    private final BookMarkRepository bookMarkRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<LessonContent> search(String keyword, Long lessonId) {
        return lessonContentRepository.searchDictionary(normalizeKeyword(keyword), lessonId);
    }

    @Transactional(readOnly = true)
    public Page<LessonContent> search(String keyword, Long lessonId, Pageable pageable) {
        return lessonContentRepository.searchDictionary(normalizeKeyword(keyword), lessonId, pageable);
    }

    @Transactional(readOnly = true)
    public List<BookMark> getBookmarks(Long userId) {
        return bookMarkRepository.findByUser_IdOrderByBookmarkedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Set<Long> getBookmarkedContentIds(Long userId) {
        return getBookmarks(userId).stream()
                .map(bookmark -> bookmark.getContent().getId())
                .collect(Collectors.toSet());
    }

    @Transactional
    public void addBookmark(Long userId, Long contentId) {
        if (bookMarkRepository.existsByUser_IdAndContent_Id(userId, contentId)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nguoi dung"));
        LessonContent content = lessonContentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tu vung"));

        BookMark bookmark = BookMark.builder()
                .user(user)
                .content(content)
                .bookmarkedAt(LocalDateTime.now())
                .build();
        bookMarkRepository.save(bookmark);
    }

    @Transactional
    public void removeBookmark(Long userId, Long contentId) {
        bookMarkRepository.deleteByUser_IdAndContent_Id(userId, contentId);
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? null : keyword.trim();
    }
}
