package com.edu.espp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http
                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(
                                                                "/",
                                                                "/login",
                                                                "/register",
                                                                "/forgot-password",
                                                                "/reset-password",
                                                                "/css/**",
                                                                "/images/**")
                                                .permitAll()

                                                // Chỉ ADMIN được truy cập
                                                .requestMatchers("/admin/**")
                                                .hasRole("ADMIN")

                                                // Chỉ STAFF được truy cập
                                                .requestMatchers("/staff/**")
                                                .hasAnyRole("STAFF", "ADMIN")
                                                // Chỉ STUDENT được truy cập
                                                .requestMatchers(
                                                                "/student/**",
                                                                "/dashboard",
                                                                "/histories/**")
                                                .hasRole("STUDENT")

                                                // Trang dùng chung cho ADMIN và STUDENT
                                                .requestMatchers("/exams/**")
                                                .hasAnyRole("ADMIN", "STUDENT", "STAFF")

                                                // Các URL còn lại cần đăng nhập
                                                .anyRequest()
                                                .authenticated())

                                .formLogin(form -> form

                                                // Trang chứa form đăng nhập
                                                .loginPage("/login")

                                                // URL Spring Security tiếp nhận form
                                                .loginProcessingUrl("/login")

                                                // Phải khớp với thuộc tính name trong login.html
                                                .usernameParameter("email")
                                                .passwordParameter("password")

                                                .successHandler((request, response, authentication) -> {

                                                        boolean isAdmin = authentication
                                                                        .getAuthorities()
                                                                        .stream()
                                                                        .anyMatch(authority -> authority.getAuthority()
                                                                                        .equals("ROLE_ADMIN"));

                                                        boolean isStaff = authentication
                                                                        .getAuthorities()
                                                                        .stream()
                                                                        .anyMatch(authority -> authority.getAuthority()
                                                                                        .equals("ROLE_STAFF"));

                                                        if (isAdmin) {
                                                                response.sendRedirect("/admin/exams");
                                                        } else if (isStaff) {
                                                                response.sendRedirect("/staff/lessons");
                                                        } else {
                                                                response.sendRedirect("/dashboard");
                                                        }
                                                })

                                                // Đăng nhập sai
                                                .failureUrl("/login?error")

                                                .permitAll()

                                )

                                .logout(logout -> logout

                                                // Form logout gửi POST đến URL này
                                                .logoutUrl("/logout")

                                                // Sau khi logout
                                                .logoutSuccessUrl("/login?logout")

                                                // Hủy session
                                                .invalidateHttpSession(true)

                                                // Xóa thông tin đăng nhập
                                                .clearAuthentication(true)

                                                // Xóa cookie session
                                                .deleteCookies("JSESSIONID")

                                                .permitAll());

                return http.build();
        }
}
