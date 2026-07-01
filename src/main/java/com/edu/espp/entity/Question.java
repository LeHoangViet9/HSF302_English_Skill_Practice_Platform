package com.edu.espp.entity;
import com.edu.espp.common.enums.QuestionSkill;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "questions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionSkill skill; // LISTENING, READING, SPEAKING, WRITING

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String questionText;

    private String audioUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String options;

    @Column(nullable = false)
    private String correctAnswer;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String explanation;
}
