package com.edu.espp.entity;
import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "lessons")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tiêu đề bài học không được để trống")
    @Size(max = 255, message = "Tiêu đề bài học tối đa 255 ký tự")
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;

    @NotNull(message = "Vui lòng chọn cấp độ bài học")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LevelLesson level; // A1, A2, B1, B2, C1, C2

    @NotNull(message = "Vui lòng chọn loại bài học")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeLesson type; // GRAMMAR, VOCABULARY, PRONUNCIATION

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;

    @Builder.Default
    private Boolean isPublished = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
