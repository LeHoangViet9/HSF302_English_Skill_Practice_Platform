package com.edu.espp.controller;

import com.edu.espp.common.enums.Role;
import com.edu.espp.dto.exam.response.ExamResponse;
import com.edu.espp.entity.User;
import com.edu.espp.service.exam.ExamService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller("commonExamController")
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public String getAllExams(HttpSession session, 
                              @RequestParam(value = "role", required = false) String roleParam, 
                              Model model) {
        // 1. Lấy thông tin người dùng đang đăng nhập từ Session
        User user = (User) session.getAttribute("currentUser");

        // 2. Logic dự phòng (Fallback / Mocking): Nếu chưa có user trong session (do chưa làm module đăng nhập / Spring Security)
        // hoặc khi dev muốn chuyển đổi nhanh vai trò bằng query parameter (?role=ADMIN hoặc ?role=STUDENT)
        if (user == null) {
            Role role = Role.STUDENT; // Vai trò mặc định là STUDENT
            if (roleParam != null) {
                try {
                    role = Role.valueOf(roleParam.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Bỏ qua nếu giá trị vai trò không khớp enum
                }
            }
            user = User.builder()
                    .id(1L)
                    .email(role == Role.ADMIN ? "admin@espp.com" : "student@espp.com")
                    .fullName(role == Role.ADMIN ? "Quản trị viên (Mock)" : "Học viên (Mock)")
                    .role(role)
                    .build();
        } else if (roleParam != null) {
            // Cho phép ghi đè vai trò từ query parameter để thuận tiện trong quá trình phát triển
            try {
                user.setRole(Role.valueOf(roleParam.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Bỏ qua
            }
        }

        // 3. Lấy danh sách tất cả đề thi từ ExamService
        List<ExamResponse> exams = examService.getAllExams();

        // 4. Truyền đối tượng user và exams sang Thymeleaf
        model.addAttribute("user", user);
        model.addAttribute("exams", exams);

        // Trả về file templates/exam/list.html chung duy nhất
        return "exam/list";
    }
}
