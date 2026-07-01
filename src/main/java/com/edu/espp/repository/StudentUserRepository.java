package com.edu.espp.repository;

import com.edu.espp.entity.StudentUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository plan: .sdd/Spect/Backend/feat-auth/001-data-model-and-migration.md §11.1
 */
public interface StudentUserRepository extends JpaRepository<StudentUser, Long> {

    Optional<StudentUser> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<StudentUser> findByEmailAndIsDeletedFalse(String email);
}
