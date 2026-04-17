package com.example.kidsfashion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/cart/add",
                                "/cart/update",
                                "/cart/remove",
                                "/cart/apply-coupon",
                                "/cart/remove-coupon",
                                "/resend-otp"  // AJAX resend không gửi CSRF token
                        )
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/index", "/test", "/error",
                                "/products/**", "/product/**", "/categories/**", "/search",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/uploads/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/register", "/verify-otp", "/resend-otp").permitAll()
                        .requestMatchers("/logout-success").permitAll()
                        .requestMatchers("/cart/add", "/cart/api/**", "/cart/update",
                                "/cart/remove", "/cart/apply-coupon",
                                "/cart/remove-coupon", "/cart/summary").permitAll()
                        .requestMatchers("/cart", "/checkout/**").authenticated()
                        .requestMatchers("/admin/reviews/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/?login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(customAuthenticationFailureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/logout-success")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request,
                                                HttpServletResponse response,
                                                org.springframework.security.core.AuthenticationException exception)
                    throws IOException, ServletException {

                // Kiểm tra nếu là tài khoản chưa xác thực email
                if (exception instanceof DisabledException) {
                    String requestedWith = request.getHeader("X-Requested-With");
                    String email = request.getParameter("username"); // username = email

                    if ("XMLHttpRequest".equals(requestedWith)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                            "{\"success\":false,\"message\":\"" + exception.getMessage() + "\"," +
                            "\"requireOtp\":true,\"email\":\"" + (email != null ? email : "") + "\"}"
                        );
                    } else {
                        String encodedEmail = email != null ?
                            URLEncoder.encode(email, StandardCharsets.UTF_8) : "";
                        response.sendRedirect("/verify-otp?email=" + encodedEmail +
                            "&msg=unverified");
                    }
                    return;
                }

                // Lỗi username/password thông thường
                String errorMessage = "Sai tên đăng nhập hoặc mật khẩu";
                String requestedWith = request.getHeader("X-Requested-With");

                if ("XMLHttpRequest".equals(requestedWith)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"" + errorMessage + "\"}");
                } else {
                    response.sendRedirect("/?error=true");
                }
            }
        };
    }
}