package com.edu.espp.repository;

import com.edu.espp.entity.LessonContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonContentRepository extends JpaRepository<LessonContent,Long> {
}
