package com.jorgeandreu.products.infrastructure.api;

import org.springframework.web.context.request.NativeWebRequest;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ApiUtil {
    private ApiUtil() {}

    public static void setExampleResponse(NativeWebRequest req, String contentType, String example) {
        try {
            HttpServletResponse res = req.getNativeResponse(HttpServletResponse.class);
            if (res != null && !res.isCommitted()) {
                res.setCharacterEncoding("UTF-8");
                res.setContentType(contentType);
                res.getWriter().print(example);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}