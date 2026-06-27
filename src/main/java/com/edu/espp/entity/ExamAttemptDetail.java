package com.edu.espp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_attemp_detail")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamAttemptDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_history_id", nullable = false)
    private ExamHistory examHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(length = 255)
    private String selectedAnswer;

    private Boolean isCorrect;
}
