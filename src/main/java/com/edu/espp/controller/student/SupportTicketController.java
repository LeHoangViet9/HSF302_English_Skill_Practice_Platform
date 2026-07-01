package com.edu.espp.controller.student;

import com.edu.espp.dto.supportTicket.request.SupportTicketRequest;
import com.edu.espp.dto.supportTicket.response.SupportTicketResponse;
import com.edu.espp.service.supportticket.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/student/support-tickets")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @GetMapping("/user/{userId}")
    public String getUserTickets(@PathVariable Long userId, Model model) {
        List<SupportTicketResponse> tickets = supportTicketService.getUserTickets(userId);
        model.addAttribute("tickets", tickets);
        model.addAttribute("userId", userId);
        return "student/support/user-ticket";
    }

    @GetMapping("/user/{userId}/new")
    public String showCreateTicketForm(@PathVariable Long userId, Model model) {
        SupportTicketRequest request = new SupportTicketRequest();
        request.setUserId(userId);

        model.addAttribute("ticketRequest", request);
        model.addAttribute("userId", userId);
        return "student/support/create-ticket";
    }

    @PostMapping("/user/{userId}")
    public String createTicket(
            @PathVariable Long userId,
            @Valid @ModelAttribute("ticketRequest") SupportTicketRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("userId", userId);
            return "student/support/create-ticket";
        }

        supportTicketService.createTicket(request);
        redirectAttributes.addFlashAttribute("successMessage", "Gửi yêu cầu hỗ trợ thành công!");

        return "redirect:/student/support-tickets/user/" + userId;
    }
}