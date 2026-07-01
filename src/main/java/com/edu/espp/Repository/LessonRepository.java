package com.edu.espp.Repository;

import com.edu.espp.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    Optional<Lesson> findFirstByOrderByIdAsc();

    Optional<Lesson> findFirstByIdGreaterThanOrderByIdAsc(Long id);
}
