package com.we4lead.backend.controller;

import com.we4lead.backend.dto.MedecinResponse;
import com.we4lead.backend.dto.UniversiteResponse;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.service.UserService;
import com.we4lead.backend.Repository.UniversiteRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/users")
public class PublicUserController {

    private final UserService userService;
    private final UniversiteRepository universiteRepository;

    public PublicUserController(UserService userService,
                                UniversiteRepository universiteRepository) {
        this.userService = userService;
        this.universiteRepository = universiteRepository;
    }

    /* ================= MEDECINS ================= */

    @GetMapping("/medecins")
    public List<MedecinResponse> getAllMedecins() {
        return userService.getMedecinsWithCreneaux();
    }

    /* ================= UNIVERSITIES ================= */

    @GetMapping("/universites")
    public List<UniversiteResponse> getAllUniversites() {
        return universiteRepository.findAll()
                .stream()
                .map(u -> new UniversiteResponse(
                        u.getId(),
                        u.getNom(),
                        u.getVille(),
                        u.getAdresse(),
                        u.getTelephone(),
                        u.getNbEtudiants(),
                        u.getHoraire(),
                        u.getLogoPath() != null ? "/uploads/" + u.getLogoPath() : null,
                        u.getCode()
                ))
                .toList();
    }

}
