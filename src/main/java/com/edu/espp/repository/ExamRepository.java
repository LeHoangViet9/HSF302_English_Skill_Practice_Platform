package com.edu.espp.repository;

import com.edu.espp.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import com.edu.espp.common.enums.ApprovalStatus;
import com.edu.espp.common.enums.TypeQuiz;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, Long id);

    List<Exam> findByTitleContainingIgnoreCase(String title);

    List<Exam> findByType(TypeQuiz type);

    List<Exam> findByTitleContainingIgnoreCaseAndType(String title, TypeQuiz type);

    org.springframework.data.domain.Page<Exam> findByApprovalStatus(com.edu.espp.common.enums.ApprovalStatus status,
            org.springframework.data.domain.Pageable pageable);

    List<Exam> findByApprovalStatus(com.edu.espp.common.enums.ApprovalStatus status);

    Optional<Exam> findByIdAndApprovalStatus(
            Long id,
            ApprovalStatus approvalStatus);
}
