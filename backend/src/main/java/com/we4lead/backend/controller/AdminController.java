package com.we4lead.backend.controller;

import com.we4lead.backend.dto.MedecinResponse;
import com.we4lead.backend.dto.UserCreateRequest;
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
}