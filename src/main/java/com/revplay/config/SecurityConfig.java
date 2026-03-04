package com.revplay.config;

import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    private final RevPlayAuthenticationEntryPoint authEntryPoint;
    private final RevPlayAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                .authorizeHttpRequests(auth -> auth
                        // ✅ Public pages
                        .requestMatchers(
                                "/", "/auth/register", "/auth/login",
                                "/css/**", "/js/**", "/images/**",
                                "/api/auth/**",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/search", "/artist/*"
                        ).permitAll()


                        // GET — listeners can browse
                        .requestMatchers(HttpMethod.GET, "/api/songs/**")
                        .authenticated()

                        // Modify — only ARTIST
                        .requestMatchers(HttpMethod.POST, "/api/songs/**")
                        .hasRole("ARTIST")

                        .requestMatchers(HttpMethod.PUT, "/api/songs/**")
                        .hasRole("ARTIST")

                        .requestMatchers(HttpMethod.DELETE, "/api/songs/**")
                        .hasRole("ARTIST")

                        .requestMatchers(HttpMethod.PATCH, "/api/songs/**")
                        .hasRole("ARTIST")

                        // ✅ Artist dashboard & analytics
                        .requestMatchers("/artist/dashboard/**",
                                "/api/artists/me/**",
                                "/api/artists/analytics/**")
                        .hasRole("ARTIST")

                        // ✅ Artist album management (Day 5 — Member 5)
                        // Double-layer security: SecurityConfig + @PreAuthorize in controller
                        .requestMatchers("/api/artists/albums/**")
                        .hasRole("ARTIST")


                        // ✅ Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ✅ Everything else
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                authEntryPoint,
                                new AntPathRequestMatcher("/api/**")
                        )
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("emailOrUsername")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}