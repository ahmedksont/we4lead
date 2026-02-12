package com.we4lead.backend.controller;

import com.we4lead.backend.dto.*;
import com.we4lead.backend.entity.Rdv;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/medecins")
    public ResponseEntity<Map<String, Object>> createMedecin(@RequestBody UserCreateRequest request) {
        // Validate university ID
        if (request.getUniversiteId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'université est obligatoire"));
        }

        User medecin = adminService.createMedecin(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Médecin créé avec succès et invitation envoyée");
        response.put("medecin", medecin);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/medecins")
    public List<MedecinResponse> getAllMedecins() {
        return adminService.getAllMedecins();
    }

    @GetMapping("/medecins/{id}")
    public User getMedecin(@PathVariable String id) {
        return adminService.getMedecinById(id);
    }

    @PutMapping("/medecins/{id}")
    public User updateMedecin(@PathVariable String id, @RequestBody UserCreateRequest request) {
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

    @PostMapping("/etudiants")
    public ResponseEntity<Map<String, Object>> createEtudiant(@RequestBody UserCreateRequest request) {
        // Validate university ID
        if (request.getUniversiteId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'université est obligatoire"));
        }

        User etudiant = adminService.createEtudiant(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Étudiant créé avec succès et invitation envoyée");
        response.put("etudiant", etudiant);

        return ResponseEntity.ok(response);
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

    @PutMapping("/etudiants/{id}")
    public User updateEtudiant(@PathVariable String id, @RequestBody UserCreateRequest request) {
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