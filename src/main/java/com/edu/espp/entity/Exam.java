package com.edu.espp.entity;
import com.edu.espp.common.enums.TypeQuiz;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Table(name = "exams")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeQuiz type; // QUIZ, MOCK_TEST

    private Integer duration;
    private Integer totalQuestions;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
}
