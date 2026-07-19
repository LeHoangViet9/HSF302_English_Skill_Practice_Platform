package com.edu.espp.service.supportticket;

import com.edu.espp.common.enums.TicketStatus;
import com.edu.espp.dto.supportTicket.request.SupportTicketRequest;
import com.edu.espp.dto.supportTicket.response.SupportTicketResponse;

import java.util.List;

public interface SupportTicketService {
    SupportTicketResponse createTicket(SupportTicketRequest request);
    List<SupportTicketResponse> getUserTickets(Long userId);
    List<SupportTicketResponse> getAllTickets();
    SupportTicketResponse updateTicketStatus(Long ticketId, TicketStatus status);
}
