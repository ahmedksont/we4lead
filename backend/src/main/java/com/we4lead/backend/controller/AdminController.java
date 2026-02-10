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
@PreAuthorize("hasAuthority('ADMIN')") // Only ADMIN can access
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ===== Medecins CRUD =====

    // ===== CREATE MEDICIN + INVITE =====
    @PostMapping("/medecins")
    public ResponseEntity<Map<String, Object>> createMedecin(@RequestBody UserCreateRequest request) {
        User medecin = adminService.createMedecin(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Medecin created and invite sent");
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

    // ===== Delete with optional forceCascade =====
    @DeleteMapping("/medecins/{id}")
    public void deleteMedecin(
            @PathVariable String id,
            @RequestParam(name = "forceCascade", defaultValue = "false") boolean forceCascade
    ) {
        adminService.deleteMedecin(id, forceCascade);
    }

}
