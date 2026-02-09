package com.we4lead.backend.dto;

public record UniversiteResponse(
        Long id,
        String nom,
        String ville,
        String adresse,
        String telephone,
        Integer nbEtudiants,
        String horaire,
        String logoPath,
        String code
) {}
