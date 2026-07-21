package com.edu.espp.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "exam_histories")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    private Double score;
    private Integer correctAnswersCount;
    private Integer timeSpent;
    @Builder.Default
    private LocalDateTime testedAt = LocalDateTime.now();
}
