package com.edu.espp.repository;

import com.edu.espp.entity.LessonContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonContentRepository extends JpaRepository<LessonContent, Long> {

    List<LessonContent> findByLesson_IdOrderByContentOrderAscIdAsc(Long lessonId);

    @Query("""
            select c from LessonContent c
            join fetch c.lesson l
            where (:keyword is null or :keyword = ''
                or lower(c.wordOrStructure) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(c.meaning, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(c.explanation, '')) like lower(concat('%', :keyword, '%')))
            and (:lessonId is null or l.id = :lessonId)
            and l.isPublished = true
            order by l.level asc, l.type asc, c.contentOrder asc, c.wordOrStructure asc
            """)
    List<LessonContent> searchDictionary(
            @Param("keyword") String keyword,
            @Param("lessonId") Long lessonId
    );
}
