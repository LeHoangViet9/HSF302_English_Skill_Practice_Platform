package com.edu.espp.service;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.entity.Lesson;
import com.edu.espp.entity.LessonContent;
import com.edu.espp.entity.User;
import com.edu.espp.repository.LessonContentRepository;
import com.edu.espp.repository.LessonRepository;
import com.edu.espp.repository.UserRepository;
import com.edu.espp.common.enums.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonContentRepository lessonContentRepository;
    private final UserRepository userRepository;


    public Page<Lesson> getPendingLessons(Pageable pageable) {
        return lessonRepository.findByApprovalStatus(ApprovalStatus.PENDING, pageable);
    }


    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));
    }

    public void saveLesson(Lesson lesson) {
        if (lesson.getId() == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
                User currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
                lesson.setCreatedBy(currentUser);
                if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    lesson.setApprovalStatus(ApprovalStatus.APPROVED);
                } else {
                    lesson.setApprovalStatus(ApprovalStatus.PENDING);
                }
            }
        }
        lessonRepository.save(lesson);
    }

    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }

    public Page<Lesson> searchLessons(String keyword, TypeLesson type, LevelLesson level, Pageable pageable) {
        return lessonRepository.searchLessons(normalizeKeyword(keyword), type, level, pageable);
    }

    public List<Lesson> searchPublishedLessons(String keyword, TypeLesson type, LevelLesson level) {
        return lessonRepository.searchPublishedLessons(normalizeKeyword(keyword), type, level);
    }

    public Page<Lesson> searchPublishedLessons(String keyword, TypeLesson type, LevelLesson level, Pageable pageable) {
        return lessonRepository.searchPublishedLessons(normalizeKeyword(keyword), type, level, pageable);
    }

    public void approveLesson(Long id) {
        Lesson lesson = getLessonById(id);
        lesson.setApprovalStatus(ApprovalStatus.APPROVED);
        lessonRepository.save(lesson);
    }

    public void rejectLesson(Long id, String reason) {
        Lesson lesson = getLessonById(id);
        lesson.setApprovalStatus(ApprovalStatus.REJECTED);
        lesson.setRejectReason(reason);
        lessonRepository.save(lesson);
    }

    public List<LessonContent> getContentsByLesson(Long lessonId) {
        return lessonContentRepository.findByLesson_IdOrderByContentOrderAscIdAsc(lessonId);
    }

    public LessonContent getContentById(Long contentId) {
        return lessonContentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nội dung bài học"));
    }

    public void saveLessonContent(Long lessonId, LessonContent content) {
        Lesson lesson = getLessonById(lessonId);
        content.setLesson(lesson);
        lessonContentRepository.save(content);
    }

    public void deleteLessonContent(Long contentId) {
        lessonContentRepository.deleteById(contentId);
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? null : keyword.trim();
    }
}