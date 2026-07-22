package com.edu.espp.repository;

import com.edu.espp.common.enums.ApprovalStatus;
import com.edu.espp.common.enums.TypeQuiz;
import com.edu.espp.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    boolean existsByTitle(String title);
    boolean existsByTitleAndIdNot(String title, Long id);
    /**
     * Query động: Tìm kiếm bài thi theo keyword, loại bài thi và trạng thái duyệt.
     * - keyword = null: bỏ qua lọc theo tên
     * - type = null: bỏ qua lọc theo loại
     * - includeAllStatuses = true: lấy tất cả trạng thái
     * - includeAllStatuses = false: chỉ lấy những bài có trạng thái = approvedStatus
     */
    @Query("""
            SELECT e FROM Exam e
            WHERE (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:type IS NULL OR e.type = :type)
              AND (:includeAllStatuses = true OR e.approvalStatus = :approvedStatus)
            """)
    List<Exam> searchByFilters(
            @Param("keyword") String keyword,
            @Param("type") TypeQuiz type,
            @Param("includeAllStatuses") boolean includeAllStatuses,
            @Param("approvedStatus") ApprovalStatus approvedStatus
    );

    Page<Exam> findByApprovalStatus(ApprovalStatus status, Pageable pageable);
}
