package com.edu.espp.repository;

import com.edu.espp.common.enums.AuthTokenType;
import com.edu.espp.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthTokenRepository
        extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByTokenValueAndTokenType(
            String tokenValue,
            AuthTokenType tokenType);

    Optional<AuthToken> findByTokenValueAndTokenTypeAndRevokedAtIsNull(
            String tokenValue,
            AuthTokenType tokenType);

    List<AuthToken> findByUser_IdAndTokenTypeAndRevokedAtIsNull(
            Long userId,
            AuthTokenType tokenType);

    List<AuthToken> findByUser_IdAndTokenTypeAndCreatedAtAfterOrderByCreatedAtAsc(
            Long userId,
            AuthTokenType tokenType,
            LocalDateTime after);
}
