package com.we4lead.backend.controller;

import com.we4lead.backend.dto.RdvRequest;
import com.we4lead.backend.entity.Rdv;
import com.we4lead.backend.service.EtudiantService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/etudiant")
public class EtudiantController {

    private final EtudiantService etudiantService;

    public EtudiantController(EtudiantService etudiantService) {
        this.etudiantService = etudiantService;
    }

    @PostMapping("/rdvs")
    public Rdv bookRdv(@AuthenticationPrincipal Jwt jwt,
                       @RequestBody RdvRequest request) {
        return etudiantService.bookRdv(jwt, request);
    }

}
