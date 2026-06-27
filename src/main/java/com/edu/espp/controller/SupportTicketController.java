package com.edu.espp.controller;

import com.edu.espp.common.dto.response.ApiResponse;
import com.edu.espp.common.enums.TicketStatus;
import com.edu.espp.dto.supportTicket.request.SupportTicketRequest;
import com.edu.espp.dto.supportTicket.response.SupportTicketResponse;
import com.edu.espp.service.supportticket.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/support-tickets")
public class SupportTicketController {
    private final SupportTicketService supportTicketService;

    // Gửi yêu cầu hỗ trợ / báo lỗi (Học viên gửi từ Screen 10)
    @PostMapping
    public ResponseEntity<ApiResponse<SupportTicketResponse>> createTicket(@Valid @RequestBody SupportTicketRequest request) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Gửi yêu cầu hỗ trợ thành công",
                supportTicketService.createTicket(request),
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

    // Xem danh sách các yêu cầu đã gửi của một học viên
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<SupportTicketResponse>>> getUserTickets(@PathVariable Long userId) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy danh sách ticket thành công",
                supportTicketService.getUserTickets(userId),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    // Cập nhật trạng thái xử lý lỗi (Admin chuyển từ OPEN -> RESOLVED)
    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> updateStatus(
            @PathVariable Long ticketId,
            @RequestParam TicketStatus status) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Cập nhật trạng thái ticket thành công",
                supportTicketService.updateTicketStatus(ticketId, status),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}
