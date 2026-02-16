package com.we4lead.backend.controller;

import com.we4lead.backend.dto.MedecinResponse;
import com.we4lead.backend.dto.UniversiteResponse;
import com.we4lead.backend.dto.UniversiteWithMedecinsResponse;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.service.UserService;
import com.we4lead.backend.Repository.UniversiteRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        return userService.getMedecinsWithAppointments();
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

    /* ================= UNIVERSITIES WITH DOCTORS ================= */

    @GetMapping("/universites/with-doctors")
    public List<UniversiteWithMedecinsResponse> getAllUniversitesWithDoctors() {
        return universiteRepository.findAll()
                .stream()
                .map(this::mapToUniversiteWithMedecinsResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/universites/{universiteId}/with-doctors")
    public UniversiteWithMedecinsResponse getUniversiteWithDoctors(@PathVariable Long universiteId) {
        Universite universite = universiteRepository.findById(universiteId)
                .orElseThrow(() -> new RuntimeException("Université non trouvée avec l'ID: " + universiteId));

        return mapToUniversiteWithMedecinsResponse(universite);
    }

    private UniversiteWithMedecinsResponse mapToUniversiteWithMedecinsResponse(Universite universite) {
        // Get all doctors (medecins) affiliated with this university
        List<MedecinResponse> doctors = universite.getMedecins().stream()
                .map(medecin -> userService.mapToMedecinResponse(medecin))
                .collect(Collectors.toList());

        return new UniversiteWithMedecinsResponse(
                universite.getId(),
                universite.getNom(),
                universite.getVille(),
                universite.getAdresse(),
                universite.getTelephone(),
                universite.getNbEtudiants(),
                universite.getHoraire(),
                universite.getLogoPath() != null ? "/uploads/" + universite.getLogoPath() : null,
                universite.getCode(),
                doctors
        );
    }
}