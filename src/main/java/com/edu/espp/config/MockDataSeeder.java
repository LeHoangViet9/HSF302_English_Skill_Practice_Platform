package com.edu.espp.config;

import com.edu.espp.entity.*;
import com.edu.espp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class MockDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final LessonContentRepository lessonContentRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final SRSReviewRepository srsReviewRepository;
    private final BookMarkRepository bookMarkRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("MockDataSeeder starting to seed full test data for all flows...");

        User student1 = userRepository.findByEmail("student1@espp.com").orElse(null);
        if (student1 == null) {
            log.warn("Student 1 not found, skipping MockDataSeeder.");
            return;
        }

        List<Lesson> lessons = lessonRepository.findAll();
        List<LessonContent> contents = lessonContentRepository.findAll();

        if (lessons.isEmpty() || contents.isEmpty()) {
            log.warn("Lessons or Contents are empty, skipping mock data.");
            return;
        }

        // 1. Seed Learning Progress
        log.info("Seeding Learning Progress...");
        for (int i = 0; i < Math.min(3, lessons.size()); i++) {
            Lesson lesson = lessons.get(i);
            if (learningProgressRepository.findByUser_IdAndLesson_Id(student1.getId(), lesson.getId()).isEmpty()) {
                learningProgressRepository.save(LearningProgress.builder()
                        .user(student1)
                        .lesson(lesson)
                        .isCompleted(i % 2 == 0) // Mix of completed and not completed
                        .updatedAt(LocalDateTime.now())
                        .build());
            }
        }

        // 2. Seed Bookmarks
        log.info("Seeding Bookmarks...");
        for (int i = 0; i < Math.min(5, contents.size()); i++) {
            LessonContent content = contents.get(i);
            if (!bookMarkRepository.existsByUser_IdAndContent_Id(student1.getId(), content.getId())) {
                bookMarkRepository.save(BookMark.builder()
                        .user(student1)
                        .content(content)
                        .bookmarkedAt(LocalDateTime.now().minusDays(i))
                        .build());
            }
        }

        // 3. Seed SRS Reviews (Flashcards)
        log.info("Seeding SRS Reviews...");
        int count = 0;
        for (LessonContent content : contents) {
            if (count >= 15) break; 
            
            if (srsReviewRepository.findByUser_IdAndContent_Id(student1.getId(), content.getId()).isEmpty()) {
                LocalDateTime nextReview = LocalDateTime.now();
                if (count % 3 == 0) {
                    nextReview = nextReview.minusDays(1); // Overdue
                } else if (count % 3 == 1) {
                    nextReview = nextReview.minusHours(1); // Due today
                } else {
                    nextReview = nextReview.plusDays(2); // Future
                }

                srsReviewRepository.save(SRSReview.builder()
                        .user(student1)
                        .content(content)
                        .repetition(count % 5)
                        .srsInterval(count % 5 == 0 ? 1 : count % 5 * 2)
                        .easeFactor(2.5)
                        .nextReviewDate(nextReview)
                        .build());
            }
            count++;
        }

        log.info("MockDataSeeder completed successfully.");
    }
}
