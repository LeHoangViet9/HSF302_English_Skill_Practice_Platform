package com.edu.espp.controller.student;


import com.edu.espp.dto.ProfileProgressResponse;
import com.edu.espp.entity.User;
import com.edu.espp.service.profile.ProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller dành cho trang cá nhân và tiến độ của học viên.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/student")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * URL:
     * GET /student/profile
     */
    @GetMapping("/profile")
    public String showProfile(
            HttpSession session,
            Model model
    ) {

        /*
         * GlobalControllerAdvice của project đã lưu user đăng nhập
         * vào session với tên "currentUser".
         */
        User currentUser =
                (User) session.getAttribute("currentUser");

        /*
         * Nếu session không có user thì chuyển về login.
         */
        if (currentUser == null) {
            return "redirect:/login";
        }

        /*
         * Chỉ cho tài khoản STUDENT truy cập.
         *
         * Nếu muốn ADMIN cũng xem được thì có thể bỏ đoạn này.
         */
        if (currentUser.getRole() == null
                || !"STUDENT".equals(currentUser.getRole().name())) {

            return "redirect:/dashboard";
        }

        /*
         * Gọi service lấy thông tin cá nhân,
         * thống kê và lịch sử thi.
         */
        ProfileProgressResponse profile =
                profileService.getProfileProgress(
                        currentUser.getId()
                );

        /*
         * Gửi dữ liệu sang profile.html.
         */
        model.addAttribute("profile", profile);

        /*
         * Trả về:
         * src/main/resources/templates/student/profile.html
         */
        return "student/profile";
    }
}