package com.edu.espp.service;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.entity.Lesson;
import com.edu.espp.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

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
}