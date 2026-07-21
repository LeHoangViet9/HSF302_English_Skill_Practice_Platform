package com.edu.espp.common.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
public class PageableUtils {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String DEFAULT_SORT_FIELD = "id";

    /**
     * Tạo đối tượng Pageable an toàn kèm theo sắp xếp động.
     *
     * @param page      Số trang (bắt đầu từ 0)
     * @param size      Kích thước trang
     * @param sortBy    Tên trường để sắp xếp (mặc định "id" nếu rỗng)
     * @param direction Hướng sắp xếp: "asc" hoặc "desc" (mặc định "desc")
     * @return Pageable an toàn với đầy đủ tham số
     */
    public static Pageable generate(Integer page, Integer size, String sortBy, String direction) {
        int safePage = (page != null && page >= 0) ? page : 0;
        int safeSize = (size != null && size > 0) ? size : DEFAULT_PAGE_SIZE;
        String safeSortBy = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy : DEFAULT_SORT_FIELD;
        
        Sort.Direction sortDir = Sort.Direction.DESC;
        if (direction != null && direction.equalsIgnoreCase("asc")) {
            sortDir = Sort.Direction.ASC;
        }

        return PageRequest.of(safePage, safeSize, Sort.by(sortDir, safeSortBy));
    }
}
