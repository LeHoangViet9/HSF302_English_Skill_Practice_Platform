package com.edu.espp.dto.lesson.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonContentResponse {

    private Long id;
    private Long lessonId;
    private String lessonTitle;
    private String wordOrStructure;
    private String ipa;
    private String meaning;
    private Integer contentOrder;
    private String explanation;
    private String example;
}
