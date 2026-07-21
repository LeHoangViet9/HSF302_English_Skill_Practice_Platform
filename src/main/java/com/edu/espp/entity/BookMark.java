package com.edu.espp.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "book_marks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "content_id"})
) //Một user chỉ được bookmark một từ/cấu trúc một lần.
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class BookMark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "content_id", nullable = false)
    private LessonContent content;

    @Builder.Default
    private LocalDateTime bookmarkedAt = LocalDateTime.now();

}
