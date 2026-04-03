package com.example.kidsfashion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/logout").setViewName("logout");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // CHỈ DÙNG uploadPath, KHÔNG cộng thêm user.dir
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath)
                .setCachePeriod(3600);

        // In ra để debug
        System.out.println("===== WEB CONFIG DEBUG =====");
        System.out.println("upload.path = " + uploadPath);
        System.out.println("Final resource location: file:" + uploadPath);
        System.out.println("============================");
    }
}