package com.edu.espp.dto.lesson.request;

import jakarta.validation.constraints.Min;
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
public class LessonContentRequest {

    private Long id;

    @NotBlank(message = "Nội dung / cấu trúc không được để trống")
    @Size(max = 255, message = "Nội dung / cấu trúc tối đa 255 ký tự")
    private String wordOrStructure;

    @Size(max = 100, message = "IPA tối đa 100 ký tự")
    private String ipa;

    @NotBlank(message = "Nghĩa / ghi chú ngắn không được để trống")
    @Size(max = 255, message = "Nghĩa / ghi chú ngắn tối đa 255 ký tự")
    private String meaning;

    @NotNull(message = "Thứ tự hiển thị không được để trống")
    @Min(value = 1, message = "Thứ tự hiển thị phải lớn hơn 0")
    private Integer contentOrder;

    @Size(max = 2000, message = "Giải thích tối đa 2000 ký tự")
    private String explanation;

    @Size(max = 2000, message = "Ví dụ tối đa 2000 ký tự")
    private String example;
}
