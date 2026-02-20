package com.we4lead.backend.controller;

import com.we4lead.backend.dto.*;
import com.we4lead.backend.entity.Genre;
import com.we4lead.backend.entity.Rdv;
import com.we4lead.backend.entity.Situation;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.service.AdminService;
import com.we4lead.backend.Repository.UniversiteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UniversiteRepository universiteRepository;

    public AdminController(AdminService adminService, UniversiteRepository universiteRepository) {
        this.adminService = adminService;
        this.universiteRepository = universiteRepository;
    }

    // ================= UNIVERSITES =================

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
                .collect(Collectors.toList());
    }

    // ================= MEDECINS CRUD =================

    @PostMapping(value = "/medecins", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> createMedecin(@ModelAttribute MedecinCreateRequest medecinRequest) {
        try {
            // Convertir le DTO multipart en UserCreateRequest
            UserCreateRequest request = new UserCreateRequest();
            request.setEmail(medecinRequest.getEmail());
            request.setNom(medecinRequest.getNom());
            request.setPrenom(medecinRequest.getPrenom());
            request.setTelephone(medecinRequest.getTelephone());

            // Parser les universiteIds
            List<Long> universiteIds = parseUniversiteIds(medecinRequest.getUniversiteIds());
            request.setUniversiteIds(universiteIds);

            request.setSpecialite(medecinRequest.getSpecialite());

            if (medecinRequest.getGenre() != null) {
                request.setGenre(Genre.valueOf(medecinRequest.getGenre()));
            }
            if (medecinRequest.getSituation() != null) {
                request.setSituation(Situation.valueOf(medecinRequest.getSituation()));
            }
            request.setNiveauEtude(medecinRequest.getNiveauEtude());
            request.setPhoto(medecinRequest.getPhoto());

            User medecin = adminService.createMedecin(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Médecin créé avec succès et invitation envoyée");
            response.put("medecin", medecin);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de l'upload de la photo: " + e.getMessage()));
        }
    }

    private List<Long> parseUniversiteIds(String universiteIdsStr) {
        if (universiteIdsStr == null || universiteIdsStr.isEmpty()) {
            return List.of();
        }
        try {
            // Enlever les crochets et splitter
            String cleaned = universiteIdsStr.replace("[", "").replace("]", "").replace(" ", "");
            if (cleaned.isEmpty()) {
                return List.of();
            }
            String[] parts = cleaned.split(",");
            return Arrays.stream(parts)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalArgumentException("Format invalide pour universiteIds. Utilisez [1,2,3]");
        }
    }
    @GetMapping("/medecins")
    public List<MedecinResponse> getAllMedecins() {
        return adminService.getAllMedecins();
    }

    @GetMapping("/medecins/{id}")
    public User getMedecin(@PathVariable String id) {
        return adminService.getMedecinById(id);
    }

    @PutMapping(value = "/medecins/{id}", consumes = {"multipart/form-data"})
    public User updateMedecin(
            @PathVariable String id,
            @ModelAttribute UserCreateRequest request
    ) throws IOException {
        return adminService.updateMedecin(id, request);
    }

    @DeleteMapping("/medecins/{id}")
    public void deleteMedecin(
            @PathVariable String id,
            @RequestParam(name = "forceCascade", defaultValue = "false") boolean forceCascade
    ) {
        adminService.deleteMedecin(id, forceCascade);
    }

    @GetMapping("/medecins/universite/{universiteId}")
    public List<MedecinResponse> getMedecinsByUniversite(@PathVariable Long universiteId) {
        return adminService.getMedecinsByUniversiteId(universiteId);
    }

    // ================= ETUDIANTS CRUD =================

    @PostMapping(value = "/etudiants", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> createEtudiant(@ModelAttribute UserCreateRequest request) {
        // Validate university IDs
        if (request.getUniversiteIds() == null || request.getUniversiteIds().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Au moins une université est obligatoire"));
        }

        // Validate email
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'email est obligatoire"));
        }

        // Validate nom and prenom
        if (request.getNom() == null || request.getNom().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le nom est obligatoire"));
        }
        if (request.getPrenom() == null || request.getPrenom().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le prénom est obligatoire"));
        }

        try {
            User etudiant = adminService.createEtudiant(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Étudiant créé avec succès et invitation envoyée");
            response.put("etudiant", etudiant);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/etudiants")
    public List<EtudiantResponse> getAllEtudiants() {
        return adminService.getAllEtudiants();
    }

    @GetMapping("/etudiants/{id}")
    public User getEtudiant(@PathVariable String id) {
        return adminService.getEtudiantById(id);
    }

    @GetMapping("/etudiants/universite/{universiteId}")
    public List<EtudiantResponse> getEtudiantsByUniversite(@PathVariable Long universiteId) {
        return adminService.getEtudiantsByUniversiteId(universiteId);
    }

    @PutMapping(value = "/etudiants/{id}", consumes = {"multipart/form-data"})
    public User updateEtudiant(
            @PathVariable String id,
            @ModelAttribute UserCreateRequest request
    ) throws IOException {
        return adminService.updateEtudiant(id, request);
    }

    @DeleteMapping("/etudiants/{id}")
    public void deleteEtudiant(@PathVariable String id) {
        adminService.deleteEtudiant(id);
    }

    // ================= RDV (APPOINTMENTS) CRUD =================


    @PostMapping("/rdvs")
    public ResponseEntity<Map<String, Object>> createRdv(@RequestBody RdvRequest request) {
        try {
            Rdv rdv = adminService.createRdv(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Rendez-vous créé avec succès");
            response.put("rdv", rdv);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/rdvs/{rdvId}/assign/{etudiantId}")
    public ResponseEntity<Map<String, Object>> assignEtudiantToRdv(
            @PathVariable String rdvId,
            @PathVariable String etudiantId) {
        try {
            Rdv rdv = adminService.assignEtudiantToRdv(rdvId, etudiantId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Étudiant assigné au rendez-vous avec succès");
            response.put("rdv", rdv);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/rdvs")
    public List<RdvResponse> getAllRdvs() {
        return adminService.getAllRdvs();
    }

    @GetMapping("/rdvs/{id}")
    public ResponseEntity<?> getRdvById(@PathVariable String id) {
        try {
            RdvResponse rdv = adminService.getRdvById(id);
            return ResponseEntity.ok(rdv);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/rdvs/{id}")
    public ResponseEntity<?> updateRdv(@PathVariable String id, @RequestBody RdvUpdateRequest request) {
        try {
            Rdv updated = adminService.updateRdv(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/rdvs/{id}")
    public ResponseEntity<?> deleteRdv(@PathVariable String id) {
        try {
            adminService.deleteRdv(id);
            return ResponseEntity.ok(Map.of("message", "Rendez-vous supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/rdvs/universite/{universiteId}")
    public ResponseEntity<?> getRdvsByUniversite(@PathVariable Long universiteId) {
        try {
            List<RdvResponse> rdvs = adminService.getRdvsByUniversiteId(universiteId);
            return ResponseEntity.ok(rdvs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/rdvs/medecin/{medecinId}")
    public ResponseEntity<?> getRdvsByMedecin(@PathVariable String medecinId) {
        try {
            List<RdvResponse> rdvs = adminService.getRdvsByMedecinId(medecinId);
            return ResponseEntity.ok(rdvs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/rdvs/etudiant/{etudiantId}")
    public ResponseEntity<?> getRdvsByEtudiant(@PathVariable String etudiantId) {
        try {
            List<RdvResponse> rdvs = adminService.getRdvsByEtudiantId(etudiantId);
            return ResponseEntity.ok(rdvs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/rdvs/status/{status}")
    public ResponseEntity<?> getRdvsByStatus(@PathVariable String status) {
        try {
            List<RdvResponse> rdvs = adminService.getRdvsByStatus(status);
            return ResponseEntity.ok(rdvs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}