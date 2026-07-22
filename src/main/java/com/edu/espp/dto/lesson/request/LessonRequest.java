package com.edu.espp.dto.lesson.request;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonRequest {

    private Long id;

    @NotBlank(message = "Tiêu đề bài học không được để trống")
    @Size(max = 255, message = "Tiêu đề bài học tối đa 255 ký tự")
    private String title;

    @NotNull(message = "Vui lòng chọn cấp độ bài học")
    private LevelLesson level;

    @NotNull(message = "Vui lòng chọn loại bài học")
    private TypeLesson type;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;

    private Boolean isPublished;
}
