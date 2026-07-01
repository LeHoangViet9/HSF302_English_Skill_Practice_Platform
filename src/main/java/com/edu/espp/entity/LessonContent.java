package com.edu.espp.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "lesson_contents")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LessonContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private String wordOrStructure;

    private String ipa;
    private String meaning;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String explanation;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String example;
}
