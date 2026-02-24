package com.we4lead.backend.controller;

import com.we4lead.backend.dto.UniversiteRequest;
import com.we4lead.backend.dto.UniversiteResponse;
import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.dto.UserResponse;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.entity.Role;
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

    @PostMapping("/universites")
    public UniversiteResponse createUniversite(@ModelAttribute UniversiteRequest request) throws IOException {
        Universite universite = superAdminService.createUniversite(request);
        return mapToResponse(universite);
    }

    @GetMapping("/universites")
    public List<UniversiteResponse> getAllUniversites() {
        return superAdminService.getAllUniversites().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/universites/{id}")
    public UniversiteResponse getUniversiteById(@PathVariable Long id) {
        Universite universite = superAdminService.getUniversiteById(id);
        return mapToResponse(universite);
    }

    @PutMapping("/universites/{id}")
    public UniversiteResponse updateUniversite(
            @PathVariable Long id,
            @ModelAttribute UniversiteRequest request
    ) throws IOException {
        Universite universite = superAdminService.updateUniversite(id, request);
        return mapToResponse(universite);
    }

    @DeleteMapping("/universites/{id}")
    public ResponseEntity<Void> deleteUniversite(@PathVariable Long id) {
        superAdminService.deleteUniversite(id);
        return ResponseEntity.noContent().build();
    }

    // ────────────────────────────────────────────────
    //                     USERS CRUD
    // ────────────────────────────────────────────────

    /**
     * Récupère tous les utilisateurs avec leur nombre de demandes
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = superAdminService.getAllUsersWithDemandesCount();
        return ResponseEntity.ok(users);
    }

    /**
     * Récupère un utilisateur avec son nombre de demandes
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        UserResponse user = superAdminService.getUserWithDemandesCount(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Récupère les utilisateurs par rôle avec leur nombre de demandes
     */
    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String role) {
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            List<UserResponse> users = superAdminService.getUsersByRoleWithDemandesCount(userRole);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ────────────────────────────────────────────────
    //                     ADMINS CRUD
    // ────────────────────────────────────────────────

    @PostMapping("/admins")
    public ResponseEntity<User> createAdmin(@RequestBody UserCreateRequest request) {
        User created = superAdminService.createAdmin(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/admins")
    public List<User> getAllAdmins() {
        return superAdminService.getAllAdmins();
    }

    @GetMapping("/admins/{id}")
    public ResponseEntity<User> getAdminById(@PathVariable String id) {
        User admin = superAdminService.getAdminById(id);
        return ResponseEntity.ok(admin);
    }

    @PutMapping("/admins/{id}")
    public ResponseEntity<User> updateAdmin(
            @PathVariable String id,
            @RequestBody UserCreateRequest request
    ) {
        User updated = superAdminService.updateAdmin(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable String id) {
        superAdminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // ────────────────────────────────────────────────
    //              MÉTHODES UTILITAIRES
    // ────────────────────────────────────────────────

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

    // Cette méthode n'est plus utilisée car on utilise maintenant
    // superAdminService.getAllUsersWithDemandesCount() qui retourne déjà des UserResponse
    // avec le nombre de demandes. On la garde pour référence ou usage futur.
    /*
    private UserResponse mapUserToResponse(User user) {
        UniversiteResponse universiteResponse = null;
        if (user.getUniversite() != null) {
            universiteResponse = new UniversiteResponse(
                    user.getUniversite().getId(),
                    user.getUniversite().getNom(),
                    user.getUniversite().getVille(),
                    user.getUniversite().getAdresse(),
                    user.getUniversite().getTelephone(),
                    user.getUniversite().getNbEtudiants(),
                    user.getUniversite().getHoraire(),
                    urlService.getUniversiteLogoUrl(user.getUniversite().getLogoPath()),
                    user.getUniversite().getCode()
            );
        }

        List<UniversiteResponse> universitesResponses = user.getUniversites().stream()
                .map(u -> new UniversiteResponse(
                        u.getId(),
                        u.getNom(),
                        u.getVille(),
                        u.getAdresse(),
                        u.getTelephone(),
                        u.getNbEtudiants(),
                        u.getHoraire(),
                        urlService.getUniversiteLogoUrl(u.getLogoPath()),
                        u.getCode()
                ))
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getTelephone(),
                user.getRole(),
                urlService.getPhotoUrl(user.getPhotoPath()),
                user.getSpecialite(),
                user.getGenre(),
                user.getSituation(),
                user.getNiveauEtude(),
                universiteResponse,
                universitesResponses,
                0L // Nombre de demandes par défaut (sera calculé dans le service)
        );
    }
    */
}