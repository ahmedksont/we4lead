package com.we4lead.backend.controller;

import com.we4lead.backend.dto.CreneauRequest;
import com.we4lead.backend.dto.CreneauResponse;
import com.we4lead.backend.dto.CreneauUpdateRequest;
import com.we4lead.backend.entity.Rdv;
import com.we4lead.backend.service.MedecinService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medecin")
public class MedecinController {

    private final MedecinService medecinService;

    public MedecinController(MedecinService medecinService) {
        this.medecinService = medecinService;
    }

    // ================= CRENEAUX CRUD (own creneaux) =================

    @PostMapping("/creneaux")
    public ResponseEntity<Map<String, Object>> createCreneau(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreneauRequest request) {
        try {
            CreneauResponse creneau = medecinService.createCreneau(jwt, request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Créneau ajouté avec succès");
            response.put("creneau", creneau);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/creneaux")
    public ResponseEntity<?> getAllMyCreneaux(@AuthenticationPrincipal Jwt jwt) {
        try {
            List<CreneauResponse> creneaux = medecinService.getAllMyCreneaux(jwt);
            return ResponseEntity.ok(creneaux);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des créneaux"));
        }
    }

    @GetMapping("/creneaux/{creneauId}")
    public ResponseEntity<?> getCreneauById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String creneauId) {
        try {
            CreneauResponse creneau = medecinService.getCreneauById(jwt, creneauId);
            return ResponseEntity.ok(creneau);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/creneaux/jour/{jour}")
    public ResponseEntity<?> getCreneauxByJour(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String jour) {
        try {
            List<CreneauResponse> creneaux = medecinService.getCreneauxByJour(jwt, jour);
            return ResponseEntity.ok(creneaux);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des créneaux"));
        }
    }

    @PutMapping("/creneaux/{creneauId}")
    public ResponseEntity<?> updateCreneau(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String creneauId,
            @RequestBody CreneauUpdateRequest request) {
        try {
            CreneauResponse updated = medecinService.updateCreneau(jwt, creneauId, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/creneaux/{creneauId}")
    public ResponseEntity<?> deleteCreneau(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String creneauId) {
        try {
            medecinService.deleteCreneau(jwt, creneauId);
            return ResponseEntity.ok(Map.of("message", "Créneau supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/creneaux")
    public ResponseEntity<?> deleteAllMyCreneaux(@AuthenticationPrincipal Jwt jwt) {
        try {
            medecinService.deleteAllMyCreneaux(jwt);
            return ResponseEntity.ok(Map.of("message", "Tous vos créneaux ont été supprimés"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la suppression des créneaux"));
        }
    }

    // ================= RDVS =================

    @GetMapping("/rdvs")
    public ResponseEntity<?> getMyRdvs(@AuthenticationPrincipal Jwt jwt) {
        try {
            List<Rdv> rdvs = medecinService.getMyRdvs(jwt);
            return ResponseEntity.ok(rdvs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des rendez-vous"));
        }
    }

}