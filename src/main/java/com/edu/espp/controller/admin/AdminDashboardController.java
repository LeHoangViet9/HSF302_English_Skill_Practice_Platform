package com.edu.espp.controller.admin;

import com.edu.espp.dto.AdminDashboardData;
import com.edu.espp.entity.User;
import com.edu.espp.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin/dashboard")
    public ModelAndView adminDashboard(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        Long adminId = (currentUser != null && currentUser.getId() != null) ? currentUser.getId() : 1L;

        AdminDashboardData dashboard = dashboardService.getAdminDashboard(adminId);

        ModelAndView mv = new ModelAndView();
        mv.setViewName("admin/dashboard");
        mv.addObject("dashboard", dashboard);

        return mv;
    }
}
