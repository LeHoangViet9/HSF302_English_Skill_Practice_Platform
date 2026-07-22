package com.edu.espp.dto.flashcard.response;

import com.edu.espp.dto.lesson.response.LessonContentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardReviewResponse {

    private Long id;
    private LessonContentResponse content;
    private Integer repetition;
    private Integer srsInterval;
    private Double easeFactor;
    private LocalDateTime nextReviewDate;
}
