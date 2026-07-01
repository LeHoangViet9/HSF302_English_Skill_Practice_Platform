package com.edu.espp.dto.supportTicket.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketResponse {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
