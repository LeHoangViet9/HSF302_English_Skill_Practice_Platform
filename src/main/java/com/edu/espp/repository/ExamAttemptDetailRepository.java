package com.edu.espp.repository;

import com.edu.espp.entity.ExamAttemptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamAttemptDetailRepository extends JpaRepository<ExamAttemptDetail, Long> {
    List<ExamAttemptDetail> findByExamHistoryId(Long examHistoryId);
}
