package com.edu.espp.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "srs_reviews")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SRSReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "content_id", nullable = false)
    private LessonContent content;

    private Integer srsInterval = 1;
    private Double easeFactor = 2.5;
    private LocalDateTime nextReviewDate = LocalDateTime.now();
}
