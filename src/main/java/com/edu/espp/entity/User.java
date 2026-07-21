package com.edu.espp.entity;
import com.edu.espp.common.enums.Role;
import com.edu.espp.common.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, columnDefinition = "NVARCHAR(150)")
    private String fullName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role; // STUDENT, STAFF, ADMIN

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status; // ACTIVE, BANNED

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
