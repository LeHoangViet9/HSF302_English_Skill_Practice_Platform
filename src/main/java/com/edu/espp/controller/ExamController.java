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
    public String getAllExams(HttpSession session, Model model) {
        // 1. Lấy thông tin người dùng từ Session (sẽ được thay thế bằng Principal của Spring Security sau này)
        User user = (User) session.getAttribute("currentUser");

        // 2. Dự phòng khi chưa có Security: Tự động khởi tạo user mặc định nếu trống
        if (user == null) {
            user = User.builder()
                    .id(1L)
                    .email("student@espp.com")
                    .fullName("Học viên (Default)")
                    .role(Role.STUDENT)
                    .build();
            session.setAttribute("currentUser", user);
        }

        // 3. Lấy danh sách đề thi từ Service
        List<ExamResponse> exams = examService.getAllExams();

        model.addAttribute("user", user);
        model.addAttribute("exams", exams);

        // Trả về templates/exam/list.html
        return "exam/list";
    }
}
