package com.revplay.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Returns HTTP 401 Unauthorized when a request is unauthenticated.
 * Triggered by: missing/expired session, bad credentials.
 *
 * Registered in SecurityConfig:
 *   http.exceptionHandling(ex -> ex
 *       .authenticationEntryPoint(authEntryPoint)
 *       ...
 *   );
 */
@Component
public class RevPlayAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(RevPlayAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        log.warn("Unauthenticated access | path={} | reason={}", request.getRequestURI(), ex.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\"," +
                        "\"message\":\"Authentication is required to access this resource.\"," +
                        "\"path\":\"%s\"}",
                LocalDateTime.now(), request.getRequestURI()
        ));
    }
}