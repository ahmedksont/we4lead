package com.we4lead.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Génère l'URL complète pour le logo d'une université
     */
    public String getUniversiteLogoUrl(String logoPath) {
        if (logoPath == null || logoPath.isEmpty()) {
            return null;
        }

        // Si c'est déjà une URL complète, la retourner directement
        if (logoPath.startsWith("http")) {
            return logoPath;
        }

        // Construire l'URL complète
        return baseUrl + "/universites/logos/" + logoPath;
    }
}