package com.edu.espp.repository;

import com.edu.espp.entity.BookMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Long> {

    List<BookMark> findByUser_IdOrderByBookmarkedAtDesc(Long userId);

    Optional<BookMark> findByUser_IdAndContent_Id(Long userId, Long contentId);

    boolean existsByUser_IdAndContent_Id(Long userId, Long contentId);

    void deleteByUser_IdAndContent_Id(Long userId, Long contentId);
}
