package com.edu.espp.service.supportticket.impl;

import com.edu.espp.common.enums.TicketStatus;
import com.edu.espp.common.exception.ResourceNotFoundException;
import com.edu.espp.dto.supportTicket.request.SupportTicketRequest;
import com.edu.espp.dto.supportTicket.response.SupportTicketResponse;
import com.edu.espp.entity.SupportTicket;
import com.edu.espp.entity.User;
import com.edu.espp.repository.SupportTicketRepository;
import com.edu.espp.repository.UserRepository;
import com.edu.espp.service.supportticket.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {
    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public SupportTicketResponse createTicket(SupportTicketRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getUserId()));

        SupportTicket ticket = SupportTicket.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        return convertToResponse(supportTicketRepository.save(ticket));
    }

    @Override
    public List<SupportTicketResponse> getUserTickets(Long userId) {
        List<SupportTicket> tickets = supportTicketRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return tickets.stream().map(this::convertToResponse).toList();
    }

    @Override
    public List<SupportTicketResponse> getAllTickets() {
        List<SupportTicket> tickets = supportTicketRepository.findAllByOrderByCreatedAtDesc();
        return tickets.stream().map(this::convertToResponse).toList();
    }

    @Override
    @Transactional
    public SupportTicketResponse updateTicketStatus(Long ticketId, TicketStatus status) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ticket yêu cầu hỗ trợ"));

        ticket.setStatus(status);
        SupportTicketResponse response = convertToResponse(supportTicketRepository.save(ticket));

        if (ticket.getUser() != null) {
            String destination = "/topic/user/" + ticket.getUser().getId() + "/notifications";
            String notificationMessage = "Ticket '" + ticket.getTitle() + "' của bạn đã được cập nhật thành trạng thái: " + status;

            messagingTemplate.convertAndSend(destination, notificationMessage);
        }

        return response;
    }

    private SupportTicketResponse convertToResponse(SupportTicket ticket) {
        if (ticket == null) return null;
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .userId(ticket.getUser() != null ? ticket.getUser().getId() : null)
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus() != null ? ticket.getStatus().name() : null)
                .createdAt(ticket.getCreatedAt())
                .build();
    }
}
