package com.we4lead.backend.service;

import com.we4lead.backend.Repository.CreneauRepository;
import com.we4lead.backend.Repository.RdvRepository;
import com.we4lead.backend.Repository.UserRepository;
import com.we4lead.backend.SupabaseAuthService;
import com.we4lead.backend.dto.CreneauResponse;
import com.we4lead.backend.dto.MedecinResponse;
import com.we4lead.backend.dto.RdvResponse;
import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.entity.Rdv;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final RdvRepository rdvRepository;
    private final CreneauRepository creneauRepository;
    private final SupabaseAuthService supabaseAuthService; // <-- injected for invites

    public AdminService(
            UserRepository userRepository,
            RdvRepository rdvRepository,
            CreneauRepository creneauRepository,
            SupabaseAuthService supabaseAuthService

    ) {
        this.userRepository = userRepository;
        this.rdvRepository = rdvRepository;
        this.creneauRepository = creneauRepository;
        this.supabaseAuthService = supabaseAuthService;
    }

    // ===== CREATE MEDICIN & INVITE =====
    @Transactional
    public User createMedecin(UserCreateRequest request) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());
        user.setRole(Role.MEDECIN);

        User savedUser = userRepository.save(user);

        // Send Supabase invite
        supabaseAuthService.inviteUser(savedUser.getEmail());

        return savedUser;
    }

    // ===== READ ALL MEDICINS =====
    public List<MedecinResponse> getAllMedecins() {
        List<User> medecins = userRepository.findByRole(Role.MEDECIN);

        return medecins.stream().map(medecin -> {
            List<CreneauResponse> creneaux = creneauRepository.findByMedecin_Id(medecin.getId())
                    .stream()
                    .map(c -> new CreneauResponse(c.getId(), c.getJour(), c.getDebut(), c.getFin()))
                    .toList();

            List<RdvResponse> rdvs = rdvRepository.findByMedecin_Id(medecin.getId())
                    .stream()
                    .map(r -> new RdvResponse(
                            r.getId(),
                            r.getDate(),
                            r.getHeure(),
                            r.getEtudiant() != null ? r.getEtudiant().getNom() : null
                    ))
                    .toList();

            return new MedecinResponse(
                    medecin.getId(),
                    medecin.getNom(),
                    medecin.getPrenom(),
                    medecin.getEmail(),
                    medecin.getPhotoPath(),
                    creneaux,
                    rdvs
            );
        }).toList();
    }

    // ===== READ ONE MEDICIN =====
    public User getMedecinById(String id) {
        return userRepository.findById(id)
                .filter(u -> u.getRole() == Role.MEDECIN)
                .orElseThrow(() -> new RuntimeException("Medecin not found"));
    }

    // ===== UPDATE MEDICIN =====
    public User updateMedecin(String id, UserCreateRequest request) {
        User user = getMedecinById(id);
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setTelephone(request.getTelephone());
        return userRepository.save(user);
    }

    // ===== DELETE CHECK =====
    public boolean canDeleteMedecin(String id) {
        List<Rdv> rdvs = rdvRepository.findByMedecin_Id(id);
        return rdvs.isEmpty();
    }

    // ===== DELETE MEDICIN =====
    @Transactional
    public void deleteMedecin(String medecinId, boolean forceCascade) {
        User medecin = getMedecinById(medecinId);
        List<Rdv> rdvs = rdvRepository.findByMedecin_Id(medecinId);

        if (!forceCascade && !rdvs.isEmpty()) {
            throw new RuntimeException("Impossible de supprimer : ce medecin a des rendez-vous !");
        }

        if (forceCascade) {
            rdvRepository.deleteAll(rdvs);
        }

        userRepository.delete(medecin);
    }
}
