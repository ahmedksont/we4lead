package com.we4lead.backend.service;

import com.we4lead.backend.dto.RdvRequest;
import com.we4lead.backend.entity.Rdv;
import com.we4lead.backend.entity.RdvStatus;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.Repository.RdvRepository;
import com.we4lead.backend.Repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EtudiantService {

    private final RdvRepository rdvRepository;
    private final UserRepository userRepository;

    public EtudiantService(RdvRepository rdvRepository, UserRepository userRepository) {
        this.rdvRepository = rdvRepository;
        this.userRepository = userRepository;
    }

    public Rdv bookRdv(Jwt jwt, RdvRequest request) {

        String etudiantId = jwt.getSubject();
        User etudiant = userRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Etudiant not found"));
        User medecin = userRepository.findById(request.getMedecinId())
                .orElseThrow(() -> new RuntimeException("Medecin not found"));

        boolean alreadyTaken =
                rdvRepository.existsByMedecin_IdAndDateAndHeure(
                        medecin.getId(),
                        request.getDate(),
                        request.getHeure()
                );

        if (alreadyTaken) {
            throw new RuntimeException("Medecin already has RDV at this time");
        }

        Rdv rdv = new Rdv();
        rdv.setId(UUID.randomUUID().toString());
        rdv.setMedecin(medecin);
        rdv.setEtudiant(etudiant);
        rdv.setDate(request.getDate());
        rdv.setHeure(request.getHeure());
        rdv.setStatus(RdvStatus.CONFIRMED);
        return rdvRepository.save(rdv);
    }
}
