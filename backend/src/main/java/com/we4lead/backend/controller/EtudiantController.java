package com.we4lead.backend.controller;

import com.we4lead.backend.dto.*;
import com.we4lead.backend.entity.Rdv;
import com.we4lead.backend.service.EtudiantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/etudiant")
public class EtudiantController {

    private final EtudiantService etudiantService;

    public EtudiantController(EtudiantService etudiantService) {
        this.etudiantService = etudiantService;
    }

    // Get all RDVs for the authenticated student
    @GetMapping("/rdvs")
    public ResponseEntity<?> getMyRdvs(@AuthenticationPrincipal Jwt jwt) {
        try {
            List<RdvResponse> rdvs = etudiantService.getMyRdvs(jwt);
            return ResponseEntity.ok(rdvs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des rendez-vous"));
        }
    }

    // Get a specific RDV by ID
    @GetMapping("/rdvs/{rdvId}")
    public ResponseEntity<?> getRdvById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String rdvId) {
        try {
            RdvResponse rdv = etudiantService.getRdvById(jwt, rdvId);
            return ResponseEntity.ok(rdv);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get RDVs by date
    @GetMapping("/rdvs/date/{date}")
    public ResponseEntity<?> getRdvsByDate(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String date) {
        try {
            List<RdvResponse> rdvs = etudiantService.getRdvsByDate(jwt, date);
            return ResponseEntity.ok(rdvs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des rendez-vous"));
        }
    }

    // Get upcoming RDVs (future dates)
    @GetMapping("/rdvs/upcoming")
    public ResponseEntity<?> getUpcomingRdvs(@AuthenticationPrincipal Jwt jwt) {
        try {
            List<RdvResponse> rdvs = etudiantService.getUpcomingRdvs(jwt);
            return ResponseEntity.ok(rdvs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des rendez-vous à venir"));
        }
    }

    // Get past RDVs
    @GetMapping("/rdvs/past")
    public ResponseEntity<?> getPastRdvs(@AuthenticationPrincipal Jwt jwt) {
        try {
            List<RdvResponse> rdvs = etudiantService.getPastRdvs(jwt);
            return ResponseEntity.ok(rdvs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des rendez-vous passés"));
        }
    }

    // Book a new RDV
    @PostMapping("/rdvs")
    public ResponseEntity<?> bookRdv(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody RdvRequest request) {
        try {
            RdvResponse rdv = etudiantService.bookRdv(jwt, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(rdv);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Cancel an RDV (student cancels their own RDV)
    @PutMapping("/rdvs/{rdvId}/cancel")
    public ResponseEntity<?> cancelRdv(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String rdvId) {
        try {
            RdvResponse rdv = etudiantService.cancelRdv(jwt, rdvId);
            return ResponseEntity.ok(rdv);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Update an RDV (reschedule)
    @PutMapping("/rdvs/{rdvId}")
    public ResponseEntity<?> updateRdv(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String rdvId,
            @RequestBody RdvUpdateRequest request) {
        try {
            RdvResponse rdv = etudiantService.updateRdv(jwt, rdvId, request);
            return ResponseEntity.ok(rdv);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Delete an RDV (permanent deletion, only if cancelled or completed)
    @DeleteMapping("/rdvs/{rdvId}")
    public ResponseEntity<?> deleteRdv(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String rdvId) {
        try {
            etudiantService.deleteRdv(jwt, rdvId);
            return ResponseEntity.ok(Map.of("message", "Rendez-vous supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    // Add this to EtudiantController.java

    @GetMapping("/university")
    public ResponseEntity<?> getMyUniversity(@AuthenticationPrincipal Jwt jwt) {
        try {
            UniversiteResponse university = etudiantService.getMyUniversity(jwt);
            return ResponseEntity.ok(university);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de l'université"));
        }
    }
    @PostMapping("/university/assign")
    public ResponseEntity<?> assignUniversity(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody AssignUniversityRequest request) {
        try {
            UniversiteResponse university = etudiantService.assignUniversity(jwt, request.getUniversityId());
            return ResponseEntity.ok(Map.of(
                    "message", "Université assignée avec succès",
                    "university", university
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'assignation de l'université"));
        }
    }

    // Optional: Remove university from student
    @DeleteMapping("/university/{universityId}")
    public ResponseEntity<?> removeUniversity(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long universityId) {
        try {
            return etudiantService.removeUniversity(jwt, universityId);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}