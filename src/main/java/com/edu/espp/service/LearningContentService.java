package com.edu.espp.service;

import com.edu.espp.entity.LearningProgress;
import com.edu.espp.entity.Lesson;
import com.edu.espp.entity.User;
import com.edu.espp.repository.LearningProgressRepository;
import com.edu.espp.repository.LessonRepository;
import com.edu.espp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningContentService {

    private final LearningProgressRepository learningProgressRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    @Transactional
    public void markCompleted(Long userId, Long lessonId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nguoi dung"));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay bai hoc"));

        LearningProgress progress = learningProgressRepository
                .findByUser_IdAndLesson_Id(userId, lessonId)
                .orElseGet(() -> LearningProgress.builder()
                        .user(user)
                        .lesson(lesson)
                        .build());

        progress.setIsCompleted(true);
        progress.setUpdatedAt(LocalDateTime.now());
        learningProgressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public Set<Long> getCompletedLessonIds(Long userId) {
        return learningProgressRepository.findByUser_IdAndIsCompletedTrue(userId).stream()
                .map(progress -> progress.getLesson().getId())
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public boolean isCompleted(Long userId, Long lessonId) {
        return learningProgressRepository.existsByUser_IdAndLesson_IdAndIsCompletedTrue(userId, lessonId);
    }
}
