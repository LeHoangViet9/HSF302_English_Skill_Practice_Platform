package com.edu.espp.repository;

import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExamId(Long examId);

    /**
     * Query động: lọc câu hỏi theo examId và/hoặc skill.
     * Nếu truyền null vào tham số nào thì điều kiện đó bị bỏ qua (IS NULL = true → lấy hết).
     * Thay thế cho 3 method riêng: findByExamId(page), findBySkill, findByExamIdAndSkill
     */
    @Query("""
            SELECT q FROM Question q
            WHERE (:examId IS NULL OR q.exam.id = :examId)
              AND (:skill  IS NULL OR q.skill  = :skill)
            """)
    Page<Question> findByFilters(
            @Param("examId") Long examId,
            @Param("skill")  QuestionSkill skill,
            Pageable pageable
    );
}
