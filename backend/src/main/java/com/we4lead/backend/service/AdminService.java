package com.we4lead.backend.service;

import com.we4lead.backend.Repository.CreneauRepository;
import com.we4lead.backend.Repository.RdvRepository;
import com.we4lead.backend.Repository.UniversiteRepository;
import com.we4lead.backend.Repository.UserRepository;
import com.we4lead.backend.SupabaseAuthService;
import com.we4lead.backend.dto.CreneauResponse;
import com.we4lead.backend.dto.MedecinResponse;
import com.we4lead.backend.dto.RdvResponse;
import com.we4lead.backend.dto.UniversiteResponse;
import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final UniversiteRepository universiteRepository;
    private final CreneauRepository creneauRepository;
    private final RdvRepository rdvRepository;
    private final SupabaseAuthService supabaseAuthService;

    public AdminService(
            UserRepository userRepository,
            UniversiteRepository universiteRepository,
            CreneauRepository creneauRepository,
            RdvRepository rdvRepository,
            SupabaseAuthService supabaseAuthService) {
        this.userRepository = userRepository;
        this.universiteRepository = universiteRepository;
        this.creneauRepository = creneauRepository;
        this.rdvRepository = rdvRepository;
        this.supabaseAuthService = supabaseAuthService;
    }

    @Transactional
    public User createMedecin(UserCreateRequest request) {
        // Validate university ID
        if (request.getUniversiteId() == null) {
            throw new IllegalArgumentException("L'université est obligatoire pour créer un médecin");
        }

        // Find the university
        Universite universite = universiteRepository.findById(request.getUniversiteId())
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + request.getUniversiteId()));

        // Create new medicin user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());
        user.setRole(Role.MEDECIN);

        // Assign university to medicin (many-to-many)
        Set<Universite> universites = new HashSet<>();
        universites.add(universite);
        user.setUniversites(universites);

        // Save the user
        User savedUser = userRepository.save(user);

        // Invite the user via Supabase
        supabaseAuthService.inviteUser(savedUser.getEmail());

        return savedUser;
    }

    public List<MedecinResponse> getAllMedecins() {
        List<User> medecins = userRepository.findByRole(Role.MEDECIN);

        return medecins.stream().map(m -> {
            List<CreneauResponse> creneaux = creneauRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(c -> new CreneauResponse(c.getId(), c.getJour(), c.getDebut(), c.getFin()))
                    .toList();

            List<RdvResponse> rdvs = rdvRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(r -> new RdvResponse(r.getId(), r.getDate(), r.getHeure(),
                            r.getEtudiant() != null ? r.getEtudiant().getNom() : null))
                    .toList();

            // Convert universities to UniversiteResponse records
            List<UniversiteResponse> universiteResponses = m.getUniversites().stream()
                    .map(u -> new UniversiteResponse(
                            u.getId(),
                            u.getNom(),
                            u.getVille(),
                            u.getAdresse(),
                            u.getTelephone(),
                            u.getNbEtudiants(),
                            u.getHoraire(),
                            u.getLogoPath(),
                            u.getCode()
                    ))
                    .toList();

            return new MedecinResponse(
                    m.getId(),
                    m.getNom(),
                    m.getPrenom(),
                    m.getEmail(),
                    m.getPhotoPath() != null ? "/users/me/photo" : null,
                    m.getTelephone(),
                    universiteResponses,
                    creneaux,
                    rdvs
            );
        }).toList();
    }

    public User getMedecinById(String id) {
        return userRepository.findById(id)
                .filter(user -> user.getRole() == Role.MEDECIN)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'ID : " + id));
    }

    @Transactional
    public User updateMedecin(String id, UserCreateRequest request) {
        User user = getMedecinById(id);

        if (request.getNom() != null) {
            user.setNom(request.getNom());
        }
        if (request.getPrenom() != null) {
            user.setPrenom(request.getPrenom());
        }
        if (request.getTelephone() != null) {
            user.setTelephone(request.getTelephone());
        }

        // Update university if provided
        if (request.getUniversiteId() != null) {
            Universite universite = universiteRepository.findById(request.getUniversiteId())
                    .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + request.getUniversiteId()));

            Set<Universite> universites = new HashSet<>();
            universites.add(universite);
            user.setUniversites(universites);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteMedecin(String id, boolean forceCascade) {
        User medecin = getMedecinById(id);

        // Check if medicin has appointments
        long appointmentsCount = rdvRepository.countByMedecin_Id(id);

        if (appointmentsCount > 0 && !forceCascade) {
            throw new RuntimeException("Ce médecin a " + appointmentsCount + " rendez-vous. Utilisez forceCascade=true pour supprimer quand même.");
        }

        // Delete related entities
        if (forceCascade) {
            rdvRepository.deleteByMedecin_Id(id);
            creneauRepository.deleteByMedecin_Id(id);
        }

        userRepository.delete(medecin);
    }
    public List<MedecinResponse> getMedecinsByUniversiteId(Long universiteId) {
        // Verify university exists
        Universite universite = universiteRepository.findById(universiteId)
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + universiteId));

        // Get all medecins for this university
        List<User> medecins = userRepository.findByUniversiteIdAndRole(universiteId, Role.MEDECIN);

        return medecins.stream().map(m -> {
            List<CreneauResponse> creneaux = creneauRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(c -> new CreneauResponse(c.getId(), c.getJour(), c.getDebut(), c.getFin()))
                    .toList();

            List<RdvResponse> rdvs = rdvRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(r -> new RdvResponse(r.getId(), r.getDate(), r.getHeure(),
                            r.getEtudiant() != null ? r.getEtudiant().getNom() : null))
                    .toList();

            // Convert universities to UniversiteResponse
            List<UniversiteResponse> universiteResponses = m.getUniversites().stream()
                    .map(u -> new UniversiteResponse(
                            u.getId(),
                            u.getNom(),
                            u.getVille(),
                            u.getAdresse(),
                            u.getTelephone(),
                            u.getNbEtudiants(),
                            u.getHoraire(),
                            u.getLogoPath(),
                            u.getCode()
                    ))
                    .toList();

            return new MedecinResponse(
                    m.getId(),
                    m.getNom(),
                    m.getPrenom(),
                    m.getEmail(),
                    m.getPhotoPath() != null ? "/users/me/photo" : null,
                    m.getTelephone(),
                    universiteResponses,
                    creneaux,
                    rdvs
            );
        }).toList();
    }
}