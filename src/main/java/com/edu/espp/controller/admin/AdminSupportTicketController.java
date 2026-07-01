package com.edu.espp.controller.admin;

import com.edu.espp.common.enums.TicketStatus;
import com.edu.espp.dto.supportTicket.response.SupportTicketResponse;
import com.edu.espp.service.supportticket.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/support-tickets")
public class AdminSupportTicketController {

    private final SupportTicketService supportTicketService;

    // 1. [Admin] Xem danh sách toàn bộ các yêu cầu hỗ trợ / báo lỗi trong hệ thống
    @GetMapping
    public String getAllTickets(Model model) {
        List<SupportTicketResponse> tickets = supportTicketService.getAllTickets();
        model.addAttribute("tickets", tickets);
        return "admin/support/list"; // Trả về templates/admin/support/list.html
    }

    // 2. [Admin] Cập nhật trạng thái xử lý lỗi (Chuyển trạng thái OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED)
    @PostMapping("/{ticketId}/update-status")
    public String updateStatus(
            @PathVariable Long ticketId,
            @RequestParam TicketStatus status,
            RedirectAttributes redirectAttributes) {

        supportTicketService.updateTicketStatus(ticketId, status);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái ticket thành công!");

        return "redirect:/admin/support-tickets";
    }
}
