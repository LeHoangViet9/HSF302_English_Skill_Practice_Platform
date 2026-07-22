package com.edu.espp.controller;

import com.edu.espp.dto.StudentDashboardData;
import com.edu.espp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


import com.edu.espp.repository.UserRepository;
import com.edu.espp.entity.User;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class StudentDashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public ModelAndView dashboard(Principal principal) {
        Long userId = 1L; // Fallback
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                userId = user.getId();
            }
        }

        StudentDashboardData dashboard = dashboardService.getStudentDashboard(userId);

        ModelAndView mv = new ModelAndView();
        mv.setViewName("student/dashboard");
        mv.addObject("dashboard", dashboard);

        return mv;
    }

}
