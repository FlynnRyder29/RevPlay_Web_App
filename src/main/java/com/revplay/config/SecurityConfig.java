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
                                        "/uploads/**",
                                        "/api/auth/**",
                                        "/swagger-ui/**", "/v3/api-docs/**",
                                        "/search", "/artist/{id:\\d+}")  // public artist profile (numeric ID only)
                                .permitAll()

                                // GET — listeners can browse
                                .requestMatchers(HttpMethod.GET, "/api/songs/**")
                                .authenticated()

                                // Modify songs — only ARTIST
                                .requestMatchers(HttpMethod.POST, "/api/songs/**").hasRole("ARTIST")
                                .requestMatchers(HttpMethod.PUT, "/api/songs/**").hasRole("ARTIST")
                                .requestMatchers(HttpMethod.DELETE, "/api/songs/**").hasRole("ARTIST")
                                .requestMatchers(HttpMethod.PATCH, "/api/songs/**").hasRole("ARTIST")

                                // ✅ Artist pages (MVC routes)
                                .requestMatchers("/artist/dashboard", "/artist/songs", "/artist/albums")
                                .hasRole("ARTIST")

                                // ✅ Artist API — management, songs, albums, analytics
                                .requestMatchers("/api/artists/me/**").hasRole("ARTIST")
                                .requestMatchers("/api/artists/songs/**").hasRole("ARTIST")
                                .requestMatchers("/api/artists/albums/**").hasRole("ARTIST")
                                .requestMatchers("/api/artists/analytics/**").hasRole("ARTIST")

                                // ✅ Artist registration — any authenticated user can register as artist
                                .requestMatchers(HttpMethod.POST, "/api/artists/register").authenticated()

                                // ✅ Public artist catalog (GET only)
                                .requestMatchers(HttpMethod.GET, "/api/artists", "/api/artists/{id:\\d+}")
                                .authenticated()

                                // ✅ Admin pages and API
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                // ✅ Everything else
                                .anyRequest().authenticated())

                                .exceptionHandling(ex -> ex
                                                .defaultAuthenticationEntryPointFor(
                                                                authEntryPoint,
                                                                new AntPathRequestMatcher("/api/**"))
                                                .accessDeniedHandler(accessDeniedHandler))

                                .formLogin(form -> form
                                                .loginPage("/auth/login")
                                                .loginProcessingUrl("/auth/login")
                                                .usernameParameter("emailOrUsername")
                                                .passwordParameter("password")
                                                .defaultSuccessUrl("/", true)
                                                .failureUrl("/auth/login?error=true")
                                                .permitAll())

                                .logout(logout -> logout
                                                .logoutUrl("/auth/logout")
                                                .logoutSuccessUrl("/auth/login?logout=true")
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())

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