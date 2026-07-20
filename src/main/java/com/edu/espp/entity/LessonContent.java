package com.edu.espp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Từ vựng hoặc cấu trúc không được để trống")
    @Size(max = 255, message = "Từ vựng hoặc cấu trúc tối đa 255 ký tự")
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String wordOrStructure;

    @Size(max = 100, message = "IPA tối đa 100 ký tự")
    @Column(columnDefinition = "NVARCHAR(100)")
    private String ipa;

    @Size(max = 255, message = "Nghĩa tối đa 255 ký tự")
    @Column(columnDefinition = "NVARCHAR(255)")
    private String meaning;

    @Min(value = 1, message = "Thứ tự hiển thị phải lớn hơn 0")
    private Integer contentOrder;

    @Size(max = 2000, message = "Giải thích tối đa 2000 ký tự")
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String explanation;

    @Size(max = 2000, message = "Ví dụ tối đa 2000 ký tự")
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String example;
}
