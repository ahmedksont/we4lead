package com.we4lead.backend.controller;

import com.we4lead.backend.dto.UniversiteRequest;
import com.we4lead.backend.dto.UniversiteResponse;
import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.service.SuperAdminService;
import com.we4lead.backend.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/superadmin")
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final UrlService urlService;

    public SuperAdminController(SuperAdminService superAdminService, UrlService urlService) {
        this.superAdminService = superAdminService;
        this.urlService = urlService;
    }

    // ────────────────────────────────────────────────
    //                  UNIVERSITES CRUD
    // ────────────────────────────────────────────────

    // CREATE
    @PostMapping("/universites")
    public UniversiteResponse createUniversite(@ModelAttribute UniversiteRequest request) throws IOException {
        Universite universite = superAdminService.createUniversite(request);
        return mapToResponse(universite);
    }

    // READ ALL
    @GetMapping("/universites")
    public List<UniversiteResponse> getAllUniversites() {
        return superAdminService.getAllUniversites().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // READ ONE
    @GetMapping("/universites/{id}")
    public UniversiteResponse getUniversiteById(@PathVariable Long id) {
        Universite universite = superAdminService.getUniversiteById(id);
        return mapToResponse(universite);
    }

    // UPDATE
    @PutMapping("/universites/{id}")
    public UniversiteResponse updateUniversite(
            @PathVariable Long id,
            @ModelAttribute UniversiteRequest request
    ) throws IOException {
        Universite universite = superAdminService.updateUniversite(id, request);
        return mapToResponse(universite);
    }

    // DELETE
    @DeleteMapping("/universites/{id}")
    public ResponseEntity<Void> deleteUniversite(@PathVariable Long id) {
        superAdminService.deleteUniversite(id);
        return ResponseEntity.noContent().build();
    }

    // ────────────────────────────────────────────────
    //                     ADMINS CRUD
    // ────────────────────────────────────────────────

    // CREATE - Invite admin + assign to one university
    @PostMapping("/admins")
    public ResponseEntity<User> createAdmin(@RequestBody UserCreateRequest request) {
        User created = superAdminService.createAdmin(request);
        return ResponseEntity.ok(created);
    }

    // READ ALL admins
    @GetMapping("/admins")
    public List<User> getAllAdmins() {
        return superAdminService.getAllAdmins();
    }

    // READ ONE admin
    @GetMapping("/admins/{id}")
    public ResponseEntity<User> getAdminById(@PathVariable String id) {
        User admin = superAdminService.getAdminById(id);
        return ResponseEntity.ok(admin);
    }

    // UPDATE admin (name, surname, phone – university change not included here)
    @PutMapping("/admins/{id}")
    public ResponseEntity<User> updateAdmin(
            @PathVariable String id,
            @RequestBody UserCreateRequest request
    ) {
        User updated = superAdminService.updateAdmin(id, request);
        return ResponseEntity.ok(updated);
    }

    // DELETE admin
    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable String id) {
        superAdminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // ────────────────────────────────────────────────
    //              MÉTHODES UTILITAIRES
    // ────────────────────────────────────────────────

    /**
     * Convertit une entité Universite en UniversiteResponse avec l'URL complète du logo
     */
    private UniversiteResponse mapToResponse(Universite universite) {
        return new UniversiteResponse(
                universite.getId(),
                universite.getNom(),
                universite.getVille(),
                universite.getAdresse(),
                universite.getTelephone(),
                universite.getNbEtudiants(),
                universite.getHoraire(),
                urlService.getUniversiteLogoUrl(universite.getLogoPath()),
                universite.getCode()
        );
    }
}