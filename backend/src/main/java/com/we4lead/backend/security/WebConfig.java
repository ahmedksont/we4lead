package com.we4lead.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // allow all endpoints
                        .allowedOrigins("http://localhost:3000") // your frontend origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // allowed HTTP methods
                        .allowedHeaders("*") // allow all headers
                        .allowCredentials(true); // allow sending cookies/auth headers if needed
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Servir les fichiers du dossier uploads/medecins
                registry.addResourceHandler("/uploads/medecins/**")
                        .addResourceLocations("file:uploads/medecins/")
                        .setCachePeriod(3600)
                        .resourceChain(true);

                // Servir les fichiers du dossier uploads/universites (pour les logos)
                registry.addResourceHandler("/uploads/universites/**")
                        .addResourceLocations("file:uploads/universites/")
                        .setCachePeriod(3600)
                        .resourceChain(true);

                // Optionnel: servir tous les uploads de manière générique
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations("file:uploads/")
                        .setCachePeriod(3600)
                        .resourceChain(true);
            }
        };
    }
}