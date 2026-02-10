package com.we4lead.backend.controller;

import com.we4lead.backend.dto.UniversiteRequest;
import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.service.SuperAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/superadmin")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    public SuperAdminController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
    }

    // ────────────────────────────────────────────────
    //                  UNIVERSITES CRUD
    // ────────────────────────────────────────────────

    // CREATE
    @PostMapping("/universites")
    public Universite createUniversite(@ModelAttribute UniversiteRequest request) throws IOException {
        return superAdminService.createUniversite(request);
    }

    // READ ALL
    @GetMapping("/universites")
    public List<Universite> getAllUniversites() {
        return superAdminService.getAllUniversites();
    }

    // READ ONE
    @GetMapping("/universites/{id}")
    public Universite getUniversiteById(@PathVariable Long id) {
        return superAdminService.getUniversiteById(id);
    }

    // UPDATE
    @PutMapping("/universites/{id}")
    public Universite updateUniversite(
            @PathVariable Long id,
            @ModelAttribute UniversiteRequest request
    ) throws IOException {
        return superAdminService.updateUniversite(id, request);
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
}