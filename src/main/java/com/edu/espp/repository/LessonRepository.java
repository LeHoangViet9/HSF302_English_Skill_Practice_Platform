package com.edu.espp.repository;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
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

    Optional<Lesson> findFirstByIdGreaterThanOrderByIdAsc(Long id);

    Optional<Lesson> findFirstByOrderByIdAsc();
}