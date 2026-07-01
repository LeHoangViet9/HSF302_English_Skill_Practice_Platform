package com.edu.espp.repository;

import com.edu.espp.common.enums.Role;
import com.edu.espp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findFirstByRole(Role role);
    Optional<User> findByEmail(String email);
}
