package com.we4lead.backend.controller;

import com.we4lead.backend.dto.CreneauRequest;
import com.we4lead.backend.entity.Creneau;
import com.we4lead.backend.entity.Rdv;
import com.we4lead.backend.service.MedecinService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medecin")
public class MedecinController {

    private final MedecinService medecinService;

    public MedecinController(MedecinService medecinService) {
        this.medecinService = medecinService;
    }
    @PostMapping("/creneaux")
    public Creneau addCreneau(@AuthenticationPrincipal Jwt jwt,
                              @RequestBody CreneauRequest request) {
        return medecinService.addCreneau(jwt, request);
    }
    @GetMapping("/rdvs")
    public List<Rdv> myRdvs(@AuthenticationPrincipal Jwt jwt) {
        return medecinService.getMyRdvs(jwt);
    }
}
