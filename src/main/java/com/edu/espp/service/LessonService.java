package com.edu.espp.service;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.entity.Lesson;
import com.edu.espp.entity.LessonContent;
import com.edu.espp.repository.LessonContentRepository;
import com.edu.espp.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonContentRepository lessonContentRepository;

    public List<Lesson> searchLessons(String keyword, TypeLesson type, LevelLesson level) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasKeyword && type != null && level != null) {
            return lessonRepository.findByTitleContainingIgnoreCaseAndTypeAndLevel(keyword, type, level);
        }

        if (hasKeyword && type != null) {
            return lessonRepository.findByTitleContainingIgnoreCaseAndType(keyword, type);
        }

        if (hasKeyword && level != null) {
            return lessonRepository.findByTitleContainingIgnoreCaseAndLevel(keyword, level);
        }

        if (type != null && level != null) {
            return lessonRepository.findByTypeAndLevel(type, level);
        }

        if (hasKeyword) {
            return lessonRepository.findByTitleContainingIgnoreCase(keyword);
        }

        if (type != null) {
            return lessonRepository.findByType(type);
        }

        if (level != null) {
            return lessonRepository.findByLevel(level);
        }

        return lessonRepository.findAll();
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

    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }

    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));
    }

    public void saveLesson(Lesson lesson) {
        lessonRepository.save(lesson);
    }

    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }

    public List<LessonContent> getContentsByLesson(Long lessonId) {
        return lessonContentRepository.findByLesson_IdOrderByContentOrderAscIdAsc(lessonId);
    }

    public LessonContent getContentById(Long contentId) {
        return lessonContentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay noi dung bai hoc"));
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
