package com.edu.espp.repository;

import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExamId(Long examId);

    List<Question> findByExamIdAndSkill(Long examId, QuestionSkill skill);


    List<Question> findBySkill(QuestionSkill skill);
}
