package com.edu.espp.dto.lesson.response;

import com.edu.espp.common.enums.ApprovalStatus;
import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResponse {

    private Long id;
    private String title;
    private LevelLesson level;
    private TypeLesson type;
    private String description;
    private Boolean isPublished;
    private ApprovalStatus approvalStatus;
    private String rejectReason;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
