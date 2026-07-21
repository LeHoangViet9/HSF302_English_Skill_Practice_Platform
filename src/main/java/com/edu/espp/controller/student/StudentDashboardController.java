package com.edu.espp.controller.student;

import com.edu.espp.common.enums.Role;
import com.edu.espp.dto.StudentDashboardData;
import com.edu.espp.entity.User;
import com.edu.espp.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class StudentDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String mainDashboard(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null && currentUser.getRole() != null) {
            if (currentUser.getRole() == Role.ADMIN) {
                return "redirect:/admin/dashboard";
            } else if (currentUser.getRole() == Role.STAFF) {
                return "redirect:/staff/dashboard";
            }
        }
        return "redirect:/student/dashboard";
    }

    @GetMapping("/student/dashboard")
    public ModelAndView studentDashboard(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        Long userId = (currentUser != null && currentUser.getId() != null) ? currentUser.getId() : 1L;

        StudentDashboardData dashboard = dashboardService.getStudentDashboard(userId);

        ModelAndView mv = new ModelAndView();
        mv.setViewName("student/dashboard");
        mv.addObject("dashboard", dashboard);

        return mv;
    }
}
