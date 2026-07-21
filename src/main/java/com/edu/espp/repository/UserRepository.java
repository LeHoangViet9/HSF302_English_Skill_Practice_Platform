package com.edu.espp.repository;

import com.edu.espp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(com.edu.espp.common.enums.Role role);

    java.util.List<User> findTop5ByOrderByIdDesc();
}