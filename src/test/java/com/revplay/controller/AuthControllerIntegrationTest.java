package com.revplay.controller;

import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.repository.UserRepository;
import com.revplay.service.AuthService;
import com.revplay.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.revplay.config.SecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AuthService authService;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler accessDeniedHandler;
    @MockitoBean private UserRepository userRepository;

    @BeforeEach
    void configureAuthEntryPoint() throws Exception {
        doAnswer(inv -> {
            jakarta.servlet.http.HttpServletResponse resp =
                    inv.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }).when(authEntryPoint).commence(any(), any(), any());
    }

    // ── GET /auth/register ────────────────────────────────────────

    @Test
    @DisplayName("GET /auth/register - returns 200 and register view")
    void getRegister_returnsRegisterView() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    @DisplayName("GET /auth/register - model contains registerRequest attribute")
    void getRegister_modelHasRegisterRequest() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    // ── POST /auth/register — success ────────────────────────────

    @Test
    @DisplayName("POST /auth/register - valid data - redirects to login")
    void postRegister_validData_redirectsToLogin() throws Exception {
        when(authService.register(any())).thenReturn(null);

        mockMvc.perform(post("/auth/register").with(csrf())
                        .param("username", "newuser")
                        .param("email", "newuser@revplay.com")
                        .param("password", "Password@123")
                        .param("confirmPassword", "Password@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    @DisplayName("POST /auth/register - valid data - calls authService.register")
    void postRegister_validData_callsAuthService() throws Exception {
        when(authService.register(any())).thenReturn(null);

        mockMvc.perform(post("/auth/register").with(csrf())
                        .param("username", "newuser")
                        .param("email", "newuser@revplay.com")
                        .param("password", "Password@123")
                        .param("confirmPassword", "Password@123"))
                .andExpect(status().is3xxRedirection());

        verify(authService).register(any());
    }

    // ── POST /auth/register — service throws ─────────────────────

    @Test
    @DisplayName("POST /auth/register - duplicate email - returns register view with error")
    void postRegister_duplicateEmail_returnsRegisterViewWithError() throws Exception {
        doThrow(new RuntimeException("Email already in use"))
                .when(authService).register(any());

        mockMvc.perform(post("/auth/register").with(csrf())
                        .param("username", "newuser")
                        .param("email", "existing@revplay.com")
                        .param("password", "Password@123")
                        .param("confirmPassword", "Password@123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));
    }

    // ── GET /auth/login ───────────────────────────────────────────

    @Test
    @DisplayName("GET /auth/login - returns 200 and login view")
    void getLogin_returnsLoginView() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}