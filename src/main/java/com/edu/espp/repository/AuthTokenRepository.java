package com.edu.espp.repository;

import com.edu.espp.entity.AuthToken;
import com.edu.espp.common.enums.AuthTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByTokenValueAndTokenType(String tokenValue, AuthTokenType tokenType);

    List<AuthToken> findByStudentUser_StudentIdAndTokenTypeAndRevokedAtIsNull(Long studentId, AuthTokenType tokenType);

    Optional<AuthToken> findByTokenValueAndTokenTypeAndRevokedAtIsNull(String tokenValue, AuthTokenType tokenType);

    /**
     * Used for the resend-verification rate limit (BR-02-05): count/inspect tokens
     * issued within the last hour, ordered oldest-first so the caller can compute
     * "retry after {X} minutes" from the earliest one.
     */
    List<AuthToken> findByStudentUser_StudentIdAndTokenTypeAndCreatedAtAfterOrderByCreatedAtAsc(
            Long studentId, AuthTokenType tokenType, LocalDateTime after);
}
