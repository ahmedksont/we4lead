package com.we4lead.backend.service;

import com.we4lead.backend.Repository.UniversiteRepository;
import com.we4lead.backend.dto.CreneauRequest;
import com.we4lead.backend.dto.CreneauResponse;
import com.we4lead.backend.dto.CreneauUpdateRequest;
import com.we4lead.backend.dto.UniversiteResponse;
import com.we4lead.backend.entity.*;
import com.we4lead.backend.Repository.CreneauRepository;
import com.we4lead.backend.Repository.RdvRepository;
import com.we4lead.backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@RequiredArgsConstructor
public class MedecinService {

    private final CreneauRepository creneauRepository;
    private final RdvRepository rdvRepository;
    private final UserRepository userRepository;
    private final UniversiteRepository universiteRepository;

    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final Set<String> VALID_DAYS = Set.of(
            "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"
    );

    // ────────────────────────────────────────────────
    //                  CRUD CRÉNEAUX
    // ────────────────────────────────────────────────

    @Transactional
    public CreneauResponse createCreneau(Jwt jwt, CreneauRequest request) {
        User medecin = getCurrentMedecin(jwt);

        validateCreneauInput(request.getJour(), request.getDebut(), request.getFin());

        String medecinId = medecin.getId();
        String jour = request.getJour();
        String debut = request.getDebut();
        String fin = request.getFin();

        if (creneauRepository.existsByMedecin_IdAndJourAndDebutAndFin(
                medecinId, jour, debut, fin)) {
            throw new IllegalArgumentException("Ce créneau existe déjà exactement");
        }

        if (creneauRepository.existsOverlapping(medecinId, jour, debut, fin, "")) {
            throw new IllegalArgumentException("Ce créneau chevauche un autre créneau existant");
        }

        Creneau creneau = new Creneau();
        creneau.setId(UUID.randomUUID().toString());
        creneau.setJour(jour);
        creneau.setDebut(debut);
        creneau.setFin(fin);
        creneau.setMedecin(medecin);

        Creneau saved = creneauRepository.save(creneau);
        return toResponse(saved);
    }

    public List<CreneauResponse> getAllMyCreneaux(Jwt jwt) {
        String medecinId = jwt.getSubject();
        return creneauRepository.findByMedecin_Id(medecinId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CreneauResponse getCreneauById(Jwt jwt, String creneauId) {
        String medecinId = jwt.getSubject();
        Creneau creneau = creneauRepository.findById(creneauId)
                .filter(c -> medecinId.equals(c.getMedecin().getId()))
                .orElseThrow(() -> new IllegalArgumentException("Créneau non trouvé ou ne vous appartient pas"));
        return toResponse(creneau);
    }

    public List<CreneauResponse> getCreneauxByJour(Jwt jwt, String jour) {
        String medecinId = jwt.getSubject();
        validateDay(jour);
        return creneauRepository.findByMedecinIdAndJour(medecinId, jour)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CreneauResponse updateCreneau(Jwt jwt, String creneauId, CreneauUpdateRequest request) {
        String medecinId = jwt.getSubject();

        Creneau creneau = creneauRepository.findById(creneauId)
                .filter(c -> medecinId.equals(c.getMedecin().getId()))
                .orElseThrow(() -> new IllegalArgumentException("Créneau non trouvé ou ne vous appartient pas"));

        boolean changed = false;

        if (isNotBlank(request.getJour())) {
            validateDay(request.getJour());
            creneau.setJour(request.getJour());
            changed = true;
        }
        if (isNotBlank(request.getDebut())) {
            validateTime(request.getDebut(), "début");
            creneau.setDebut(request.getDebut());
            changed = true;
        }
        if (isNotBlank(request.getFin())) {
            validateTime(request.getFin(), "fin");
            creneau.setFin(request.getFin());
            changed = true;
        }

        if (changed) {
            validateDebutBeforeFin(creneau.getDebut(), creneau.getFin());

            if (creneauRepository.existsOverlapping(
                    medecinId, creneau.getJour(), creneau.getDebut(), creneau.getFin(), creneauId)) {
                throw new IllegalArgumentException("Ce créneau chevauche un autre créneau existant");
            }

            creneau = creneauRepository.save(creneau);
        }

        return toResponse(creneau);
    }

    @Transactional
    public void deleteCreneau(Jwt jwt, String creneauId) {
        String medecinId = jwt.getSubject();

        // Safer version — no need for existsByIdAndMedecin_Id
        Creneau creneau = creneauRepository.findById(creneauId)
                .filter(c -> medecinId.equals(c.getMedecin().getId()))
                .orElseThrow(() -> new IllegalArgumentException("Créneau non trouvé ou ne vous appartient pas"));

        creneauRepository.delete(creneau);
    }

    @Transactional
    public void deleteAllMyCreneaux(Jwt jwt) {
        String medecinId = jwt.getSubject();
        creneauRepository.deleteByMedecin_Id(medecinId);
    }

    // ────────────────────────────────────────────────
    //                   Helpers
    // ────────────────────────────────────────────────

    private User getCurrentMedecin(Jwt jwt) {
        return userRepository.findById(jwt.getSubject())
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé"));
    }

    private void validateCreneauInput(String jour, String debut, String fin) {
        validateDay(jour);
        validateTime(debut, "début");
        validateTime(fin, "fin");
        validateDebutBeforeFin(debut, fin);
    }

    private void validateDay(String jour) {
        if (jour == null || !VALID_DAYS.contains(jour)) {
            throw new IllegalArgumentException("Jour invalide. Valeurs acceptées : " + VALID_DAYS);
        }
    }

    private void validateTime(String time, String field) {
        if (time == null || !TIME_PATTERN.matcher(time).matches()) {
            throw new IllegalArgumentException(
                    "Format d'heure invalide pour " + field + " (attendu : HH:mm, ex: 09:30)");
        }
    }

    private void validateDebutBeforeFin(String debut, String fin) {
        if (debut.compareTo(fin) >= 0) {
            throw new IllegalArgumentException(
                    "L'heure de début doit être strictement antérieure à l'heure de fin");
        }
    }

    private CreneauResponse toResponse(Creneau c) {
        return new CreneauResponse(c.getId(), c.getJour(), c.getDebut(), c.getFin());
    }
    public List<Rdv> getMyRdvs(Jwt jwt) {
        String medecinId = jwt.getSubject();
        return rdvRepository.findByMedecin_Id(medecinId);
    }
    public List<UniversiteResponse> getMyUniversities(Jwt jwt) {
        String doctorId = jwt.getSubject();

        // Verify the user is a doctor
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé"));

        if (doctor.getRole() != Role.MEDECIN) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un médecin");
        }

        List<Universite> universities = universiteRepository.findByMedecinId(doctorId);

        return universities.stream()
                .map(this::toUniversiteResponse)
                .collect(Collectors.toList());
    }

    public List<UniversiteResponse> getUniversitiesByDoctorId(String doctorId) {
        // Verify the doctor exists
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé"));

        if (doctor.getRole() != Role.MEDECIN) {
            throw new IllegalArgumentException("L'utilisateur avec l'ID " + doctorId + " n'est pas un médecin");
        }

        List<Universite> universities = universiteRepository.findByMedecinId(doctorId);

        if (universities.isEmpty()) {
            throw new IllegalArgumentException("Aucune université trouvée pour ce médecin");
        }

        return universities.stream()
                .map(this::toUniversiteResponse)
                .collect(Collectors.toList());
    }

    public UniversiteResponse getFirstUniversityByDoctorId(String doctorId) {
        // Verify the doctor exists
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé"));

        if (doctor.getRole() != Role.MEDECIN) {
            throw new IllegalArgumentException("L'utilisateur avec l'ID " + doctorId + " n'est pas un médecin");
        }

        Universite university = universiteRepository.findFirstByMedecinId(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Aucune université trouvée pour ce médecin"));

        return toUniversiteResponse(university);
    }

    private UniversiteResponse toUniversiteResponse(Universite universite) {
        return new UniversiteResponse(
                universite.getId(),
                universite.getNom(),
                universite.getVille(),
                universite.getAdresse(),
                universite.getTelephone(),
                universite.getNbEtudiants(),
                universite.getHoraire(),
                universite.getLogoPath(),
                universite.getCode()
        );
    }
}