package com.we4lead.backend.controller;

import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.service.AdminService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    @PostMapping("/medecins")
    public User createMedecin(@RequestBody UserCreateRequest request) {
        return adminService.createMedecin(request);
    }
}
