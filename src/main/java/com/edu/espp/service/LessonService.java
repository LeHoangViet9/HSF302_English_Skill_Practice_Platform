package com.edu.espp.service;

import com.edu.espp.common.enums.ApprovalStatus;
import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.dto.lesson.request.LessonContentRequest;
import com.edu.espp.dto.lesson.request.LessonRequest;
import com.edu.espp.dto.lesson.response.LessonContentResponse;
import com.edu.espp.dto.lesson.response.LessonResponse;
import com.edu.espp.entity.Lesson;
import com.edu.espp.entity.LessonContent;
import com.edu.espp.entity.User;
import com.edu.espp.repository.LessonContentRepository;
import com.edu.espp.repository.LessonRepository;
import com.edu.espp.repository.UserRepository;
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

    public LessonRequest toLessonRequest(Lesson lesson) {
        return LessonRequest.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .level(lesson.getLevel())
                .type(lesson.getType())
                .description(lesson.getDescription())
                .isPublished(lesson.getIsPublished() == null ? Boolean.TRUE : lesson.getIsPublished())
                .build();
    }

    public LessonResponse toLessonResponse(Lesson lesson) {
        User createdBy = lesson.getCreatedBy();
        return LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .level(lesson.getLevel())
                .type(lesson.getType())
                .description(lesson.getDescription())
                .isPublished(lesson.getIsPublished() == null ? Boolean.TRUE : lesson.getIsPublished())
                .approvalStatus(lesson.getApprovalStatus())
                .rejectReason(lesson.getRejectReason())
                .createdById(createdBy == null ? null : createdBy.getId())
                .createdByName(createdBy == null ? null : createdBy.getFullName())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

    public LessonContentRequest toLessonContentRequest(LessonContent content) {
        return LessonContentRequest.builder()
                .id(content.getId())
                .wordOrStructure(content.getWordOrStructure())
                .ipa(content.getIpa())
                .meaning(content.getMeaning())
                .contentOrder(content.getContentOrder())
                .explanation(content.getExplanation())
                .example(content.getExample())
                .build();
    }

    public LessonContentResponse toLessonContentResponse(LessonContent content) {
        Lesson lesson = content.getLesson();
        return LessonContentResponse.builder()
                .id(content.getId())
                .lessonId(lesson == null ? null : lesson.getId())
                .lessonTitle(lesson == null ? null : lesson.getTitle())
                .wordOrStructure(content.getWordOrStructure())
                .ipa(content.getIpa())
                .meaning(content.getMeaning())
                .contentOrder(content.getContentOrder())
                .explanation(content.getExplanation())
                .example(content.getExample())
                .build();
    }

    public List<LessonResponse> toLessonResponses(List<Lesson> lessons) {
        return lessons.stream().map(this::toLessonResponse).toList();
    }

    public List<LessonContentResponse> toLessonContentResponses(List<LessonContent> contents) {
        return contents.stream().map(this::toLessonContentResponse).toList();
    }

    public Lesson saveLesson(LessonRequest request) {
        Lesson lesson = request.getId() == null
                ? new Lesson()
                : lessonRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));

        lesson.setTitle(request.getTitle());
        lesson.setLevel(request.getLevel());
        lesson.setType(request.getType());
        lesson.setDescription(request.getDescription());

        if (request.getIsPublished() != null) {
            lesson.setIsPublished(request.getIsPublished());
        } else if (lesson.getIsPublished() == null) {
            lesson.setIsPublished(Boolean.TRUE);
        }

        if (request.getId() == null) {
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

            if (lesson.getApprovalStatus() == null) {
                lesson.setApprovalStatus(ApprovalStatus.PENDING);
            }
        }

        return lessonRepository.save(lesson);
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

    public LessonContent saveLessonContent(Long lessonId, LessonContentRequest request) {
        Lesson lesson = getLessonById(lessonId);
        LessonContent content = request.getId() == null
                ? new LessonContent()
                : lessonContentRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nội dung bài học"));

        content.setLesson(lesson);
        content.setWordOrStructure(request.getWordOrStructure());
        content.setIpa(request.getIpa());
        content.setMeaning(request.getMeaning());
        content.setContentOrder(request.getContentOrder());
        content.setExplanation(request.getExplanation());
        content.setExample(request.getExample());

        return lessonContentRepository.save(content);
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
