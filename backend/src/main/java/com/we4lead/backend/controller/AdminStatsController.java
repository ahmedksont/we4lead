package com.we4lead.backend.controller;

import com.we4lead.backend.dto.AdminStatsResponse;
import com.we4lead.backend.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/stats")
public class AdminStatsController {

    private final StatsService statsService;

    public AdminStatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<AdminStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(statsService.getAdminStats());
    }

    @GetMapping("/universite/{universiteId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AdminStatsResponse> getUniversityStats(@PathVariable Long universiteId) {
        return ResponseEntity.ok(statsService.getUniversityAdminStats(universiteId));
    }
}