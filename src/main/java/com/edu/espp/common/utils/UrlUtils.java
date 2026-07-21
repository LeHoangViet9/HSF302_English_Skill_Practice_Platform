package com.edu.espp.common.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component("urlUtils")
public class UrlUtils {

    /**
     * Replaces or adds the 'page' query parameter in the current request URL.
     * Keeps all other existing parameters (like filters/search) intact.
     *
     * @param page The new page number.
     * @return The updated URL string.
     */
    public String replacePageParam(Integer page) {
        try {
            return ServletUriComponentsBuilder.fromCurrentRequest()
                    .replaceQueryParam("page", page)
                    .build()
                    .encode()
                    .toUriString();
        } catch (Exception e) {
            // Fallback in case of exceptions or no request bound
            return "?page=" + page;
        }
    }
}
