package com.we4lead.backend.service;

import com.we4lead.backend.Repository.UserRepository;
import com.we4lead.backend.dto.CreneauRequest;
import com.we4lead.backend.entity.Creneau;
import com.we4lead.backend.entity.Rdv;
import com.we4lead.backend.Repository.CreneauRepository;
import com.we4lead.backend.Repository.RdvRepository;
import com.we4lead.backend.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Service
public class MedecinService {

    private final CreneauRepository creneauRepository;
    private final RdvRepository rdvRepository;
    private final UserRepository userRepository;

    public MedecinService(CreneauRepository creneauRepository,
                          RdvRepository rdvRepository,
                          UserRepository userRepository) {
        this.creneauRepository = creneauRepository;
        this.rdvRepository = rdvRepository;
        this.userRepository = userRepository;
    }

    public Creneau addCreneau(Jwt jwt, CreneauRequest request) {

        User medecin = userRepository.findById(jwt.getSubject())
                .orElseThrow();

        Creneau c = new Creneau();
        c.setId(UUID.randomUUID().toString());
        c.setJour(request.getJour());
        c.setDebut(request.getDebut());
        c.setFin(request.getFin());
        c.setMedecin(medecin);

        return creneauRepository.save(c);
    }

    public List<Rdv> getMyRdvs(Jwt jwt) {
        return rdvRepository.findByMedecin_Id(jwt.getSubject());
    }
}
