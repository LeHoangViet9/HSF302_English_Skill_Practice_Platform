package com.edu.espp.dto.bookmark.response;

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
public class BookmarkResponse {

    private Long id;
    private LessonContentResponse content;
    private LocalDateTime bookmarkedAt;
}
