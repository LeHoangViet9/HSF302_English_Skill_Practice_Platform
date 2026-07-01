package com.edu.espp.repository;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByTitleContainingIgnoreCase(String keyword);

    List<Lesson> findByType(TypeLesson type);

    List<Lesson> findByLevel(LevelLesson level);

    List<Lesson> findByTypeAndLevel(TypeLesson type, LevelLesson level);

    List<Lesson> findByTitleContainingIgnoreCaseAndType(String keyword, TypeLesson type);

    List<Lesson> findByTitleContainingIgnoreCaseAndLevel(String keyword, LevelLesson level);

    List<Lesson> findByTitleContainingIgnoreCaseAndTypeAndLevel(
            String keyword,
            TypeLesson type,
            LevelLesson level
    );
}