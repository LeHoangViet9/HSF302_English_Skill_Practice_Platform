package com.edu.espp.service.profile;



/**
 * Service xử lý dữ liệu trang Profile & Progress.
 */
public interface ProfileService {

    /**
     * Lấy thông tin cá nhân và tiến độ thi của một học viên.
     *
     * @param userId ID của học viên
     * @return dữ liệu profile và lịch sử thi
     */
    com.edu.espp.dto.ProfileProgressResponse getProfileProgress(Long userId);
}