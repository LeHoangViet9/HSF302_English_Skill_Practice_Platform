package com.edu.espp.controller;

import com.edu.espp.dto.StudentDashboardData;
import com.edu.espp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequiredArgsConstructor
public class StudentDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ModelAndView dashboard(jakarta.servlet.http.HttpSession session) {
        com.edu.espp.entity.User user = (com.edu.espp.entity.User) session.getAttribute("currentUser");
        Long userId = user != null ? user.getId() : 1L;

        StudentDashboardData dashboard = dashboardService.getStudentDashboard(userId);

        ModelAndView mv = new ModelAndView();
        mv.setViewName("student/dashboard");
        mv.addObject("dashboard", dashboard);

        return mv;
    }

}