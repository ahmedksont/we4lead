package com.we4lead.backend.controller;

import com.we4lead.backend.dto.UniversiteRequest;
import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.service.SuperAdminService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/superadmin")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    public SuperAdminController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
    }

    @PostMapping("/universites")
    public Universite createUniversite(@RequestBody UniversiteRequest request) {
        return superAdminService.createUniversite(request);
    }

    @PostMapping("/admins")
    public User createAdmin(@RequestBody UserCreateRequest request) {
        return superAdminService.createAdmin(request);
    }
}
