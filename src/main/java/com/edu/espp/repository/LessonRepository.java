package com.edu.espp.repository;

import com.edu.espp.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    Optional<Lesson> findFirstByOrderByIdAsc();

    @Query("select l from Lesson l where l.title = :title")
    Optional<Lesson> findByExactTitle(@Param("title") String title);

    Optional<Lesson> findFirstByIdGreaterThanOrderByIdAsc(Long id);



    @Query("""
            select l from Lesson l
            where l.approvalStatus = com.edu.espp.common.enums.ApprovalStatus.APPROVED
            and (:keyword is null or :keyword = ''
                or lower(l.title) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(l.description, '')) like lower(concat('%', :keyword, '%')))
            and (:type is null or l.type = :type)
            and (:level is null or l.level = :level)
            order by l.id asc
            """)
    List<Lesson> searchPublishedLessons(
            @Param("keyword") String keyword,
            @Param("type") TypeLesson type,
            @Param("level") LevelLesson level
    );

    @Query("""
            select l from Lesson l
            where (:keyword is null or :keyword = ''
                or lower(l.title) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(l.description, '')) like lower(concat('%', :keyword, '%')))
            and (:type is null or l.type = :type)
            and (:level is null or l.level = :level)
            """)
    Page<Lesson> searchLessons(
            @Param("keyword") String keyword,
            @Param("type") TypeLesson type,
            @Param("level") LevelLesson level,
            Pageable pageable
    );

    @Query("""
            select l from Lesson l
            where l.approvalStatus = com.edu.espp.common.enums.ApprovalStatus.APPROVED
            and (:keyword is null or :keyword = ''
                or lower(l.title) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(l.description, '')) like lower(concat('%', :keyword, '%')))
            and (:type is null or l.type = :type)
            and (:level is null or l.level = :level)
            """)
    Page<Lesson> searchPublishedLessons(
            @Param("keyword") String keyword,
            @Param("type") TypeLesson type,
            @Param("level") LevelLesson level,
            Pageable pageable
    );

    Page<Lesson> findByApprovalStatus(com.edu.espp.common.enums.ApprovalStatus status, Pageable pageable);
}
