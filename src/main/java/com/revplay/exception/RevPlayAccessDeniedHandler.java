package com.revplay.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Returns HTTP 403 Forbidden when an authenticated user lacks permission.
 * Triggered by: role mismatches, @PreAuthorize failures.
 *
 * Registered in SecurityConfig:
 *   http.exceptionHandling(ex -> ex
 *       ...
 *       .accessDeniedHandler(accessDeniedHandler)
 *   );
 */
@Component
public class RevPlayAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(RevPlayAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        log.warn("Access denied | path={} | reason={}", request.getRequestURI(), ex.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\"," +
                        "\"message\":\"You do not have permission to access this resource.\"," +
                        "\"path\":\"%s\"}",
                LocalDateTime.now(), request.getRequestURI()
        ));
    }
}