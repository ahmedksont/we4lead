package com.we4lead.backend.controller;

import com.we4lead.backend.dto.UniversiteRequest;
import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.service.SuperAdminService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/superadmin")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    public SuperAdminController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
    }

    /**
     * Create a university with optional logo upload
     * Content-Type: multipart/form-data
     */
    @PostMapping("/universites")
    public Universite createUniversite(
            @ModelAttribute UniversiteRequest request
    ) throws IOException {
        return superAdminService.createUniversite(request);
    }

    /**
     * Create an admin user
     */
    @PostMapping("/admins")
    public User createAdmin(@RequestBody UserCreateRequest request) {
        return superAdminService.createAdmin(request);
    }
}
