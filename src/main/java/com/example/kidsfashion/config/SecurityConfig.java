package com.example.kidsfashion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                                "/cart/remove-coupon"
                        )
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/index", "/test", "/error",
                                "/products/**", "/product/**", "/categories/**", "/search",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/uploads/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/register").permitAll()
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
                        .logoutUrl("/logout")  // ✅ Thay logoutRequestMatcher bằng logoutUrl
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

                String errorMessage = "Invalid username or password";

                // Kiểm tra nếu là AJAX request
                String requestedWith = request.getHeader("X-Requested-With");
                if ("XMLHttpRequest".equals(requestedWith)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"" + errorMessage + "\"}");
                } else {
                    // Request thường - redirect về trang chủ với parameter error
                    response.sendRedirect("/?error=true");
                }
            }
        };
    }
}