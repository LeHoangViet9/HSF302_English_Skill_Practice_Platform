package com.edu.espp.repository;

import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExamId(Long examId);
    Page<Question> findByExamId(Long examId, Pageable pageable);

    List<Question> findByExamIdAndSkill(Long examId, QuestionSkill skill);
    Page<Question> findByExamIdAndSkill(Long examId, QuestionSkill skill, Pageable pageable);

    Page<Question> findBySkill(QuestionSkill skill, Pageable pageable);
}
