package com.we4lead.backend.controller;

import com.we4lead.backend.dto.DemandeResponse;
import com.we4lead.backend.dto.DemandeUpdateRequest;
import com.we4lead.backend.dto.DemandeWithStudentRequest;
import com.we4lead.backend.entity.TypeSituation;
import com.we4lead.backend.service.DemandeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/demandes")
public class DemandeController {

    private final DemandeService demandeService;

    public DemandeController(DemandeService demandeService) {
        this.demandeService = demandeService;
    }

    /**
     * Endpoint public pour créer une demande avec création automatique de l'étudiant
     */
    @PostMapping("/public")
    public ResponseEntity<Map<String, Object>> createDemandePublic(@RequestBody DemandeWithStudentRequest request) {
        try {
            DemandeResponse demande = demandeService.createDemandeWithStudent(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Demande créée avec succès");
            response.put("demande", demande);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de la création de la demande: " + e.getMessage()));
        }
    }

    /**
     * Récupère les demandes par email étudiant
     */
    @GetMapping("/etudiant/{email}")
    public ResponseEntity<?> getDemandesByStudentEmail(@PathVariable String email) {
        try {
            List<DemandeResponse> demandes = demandeService.getDemandesByStudentEmail(email);
            return ResponseEntity.ok(demandes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère les demandes par ID médecin
     */
    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<?> getDemandesByMedecinId(@PathVariable String medecinId) {
        try {
            List<DemandeResponse> demandes = demandeService.getDemandesByMedecinId(medecinId);
            return ResponseEntity.ok(demandes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère les demandes d'un médecin pour une université spécifique
     */
    @GetMapping("/medecin/{medecinId}/universite/{universiteId}")
    public ResponseEntity<?> getDemandesByMedecinAndUniversite(
            @PathVariable String medecinId,
            @PathVariable Long universiteId) {
        try {
            List<DemandeResponse> demandes = demandeService.getDemandesByMedecinAndUniversite(medecinId, universiteId);
            return ResponseEntity.ok(demandes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère les demandes par université
     */
    @GetMapping("/universite/{universiteId}")
    public ResponseEntity<?> getDemandesByUniversite(@PathVariable Long universiteId) {
        try {
            List<DemandeResponse> demandes = demandeService.getDemandesByUniversiteId(universiteId);
            return ResponseEntity.ok(demandes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère toutes les demandes (pour admin)
     */
    @GetMapping("/all")
    public ResponseEntity<List<DemandeResponse>> getAllDemandes() {
        return ResponseEntity.ok(demandeService.getAllDemandes());
    }

    /**
     * Récupère une demande par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDemandeById(@PathVariable String id) {
        try {
            DemandeResponse demande = demandeService.getDemandeById(id);
            return ResponseEntity.ok(demande);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Récupère les demandes par type de situation
     */
    @GetMapping("/type/{typeSituation}")
    public ResponseEntity<?> getDemandesByType(@PathVariable TypeSituation typeSituation) {
        try {
            List<DemandeResponse> demandes = demandeService.getDemandesByTypeSituation(typeSituation);
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère les demandes par période
     */
    @GetMapping("/periode/{periode}")
    public ResponseEntity<?> getDemandesByPeriode(@PathVariable String periode) {
        try {
            List<DemandeResponse> demandes = demandeService.getDemandesByPeriode(periode);
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Supprime une demande
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDemande(@PathVariable String id) {
        try {
            demandeService.deleteDemande(id);
            return ResponseEntity.ok(Map.of("message", "Demande supprimée avec succès"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    /**
     * Modifie une demande existante (PUT complet)
     * La description n'est pas modifiable
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDemande(@PathVariable String id, @RequestBody DemandeUpdateRequest request) {
        try {
            DemandeResponse updatedDemande = demandeService.updateDemande(id, request);
            return ResponseEntity.ok(updatedDemande);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de la modification: " + e.getMessage()));
        }
    }

    /**
     * Modifie partiellement une demande (PATCH)
     * La description n'est pas modifiable
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateDemande(@PathVariable String id, @RequestBody DemandeUpdateRequest request) {
        try {
            DemandeResponse updatedDemande = demandeService.updateDemande(id, request);
            return ResponseEntity.ok(updatedDemande);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de la modification: " + e.getMessage()));
        }
    }
}