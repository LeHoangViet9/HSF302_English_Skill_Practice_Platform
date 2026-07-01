package edu.fu.entity;

import edu.fu.common.converter.AuthTokenTypeConverter;
import edu.fu.common.enums.AuthTokenType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Maps to {@code auth_tokens} (see .sdd/Spect/Backend/feat-auth/001-data-model-and-migration.md §5.3).
 * {@code tokenValue} is deliberately excluded from {@link #toString()} (AG-15: No Secret Exposure).
 */
@Entity
@Table(name = "auth_tokens", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_auth_tokens_token_value", columnNames = "token_value")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "tokenId")
@ToString(of = {"tokenId", "tokenType", "expiresAt"})
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "FK_auth_tokens_student"))
    private StudentUser studentUser;

    @Convert(converter = AuthTokenTypeConverter.class)
    @Column(name = "token_type", nullable = false, length = 30)
    private AuthTokenType tokenType;

    @Column(name = "token_value", nullable = false, length = 500)
    private String tokenValue;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }
}
