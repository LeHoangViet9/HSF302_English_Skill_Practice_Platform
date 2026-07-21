package com.edu.espp.controller.staff;

import com.edu.espp.dto.StaffDashboardData;
import com.edu.espp.entity.User;
import com.edu.espp.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class StaffDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/staff/dashboard")
    public ModelAndView staffDashboard(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        Long staffId = (currentUser != null && currentUser.getId() != null) ? currentUser.getId() : 1L;

        StaffDashboardData dashboard = dashboardService.getStaffDashboard(staffId);

        ModelAndView mv = new ModelAndView();
        mv.setViewName("staff/dashboard");
        mv.addObject("dashboard", dashboard);

        return mv;
    }
}
