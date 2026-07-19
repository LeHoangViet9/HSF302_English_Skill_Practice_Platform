package com.edu.espp.repository;

import com.edu.espp.entity.StudentUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentUserRepository
        extends JpaRepository<StudentUser, Long> {

    Optional<StudentUser> findByUser_Id(Long userId);

    Optional<StudentUser> findByUser_Email(String email);

    boolean existsByUser_Id(Long userId);
}