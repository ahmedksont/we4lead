package com.we4lead.backend.service;

import com.we4lead.backend.dto.*;
import com.we4lead.backend.entity.Rdv;
import com.we4lead.backend.entity.RdvStatus;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.Repository.RdvRepository;
import com.we4lead.backend.Repository.UserRepository;
import com.we4lead.backend.Repository.UniversiteRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EtudiantService {

    private final RdvRepository rdvRepository;
    private final UserRepository userRepository;
    private final UniversiteRepository universiteRepository;

    public EtudiantService(RdvRepository rdvRepository,
                           UserRepository userRepository,
                           UniversiteRepository universiteRepository) {
        this.rdvRepository = rdvRepository;
        this.userRepository = userRepository;
        this.universiteRepository = universiteRepository;
    }

    // Get all RDVs for the authenticated student
    public List<RdvResponse> getMyRdvs(Jwt jwt) {
        String etudiantId = jwt.getSubject();
        return rdvRepository.findByEtudiant_IdOrderByDateDescHeureDesc(etudiantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Get a specific RDV by ID
    public RdvResponse getRdvById(Jwt jwt, String rdvId) {
        String etudiantId = jwt.getSubject();
        Rdv rdv = rdvRepository.findById(rdvId)
                .filter(r -> etudiantId.equals(r.getEtudiant().getId()))
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé ou ne vous appartient pas"));
        return toResponse(rdv);
    }

    // Get RDVs by date
    public List<RdvResponse> getRdvsByDate(Jwt jwt, String date) {
        String etudiantId = jwt.getSubject();
        return rdvRepository.findByEtudiant_IdAndDateOrderByHeure(etudiantId, date)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Get upcoming RDVs (future dates)
    public List<RdvResponse> getUpcomingRdvs(Jwt jwt) {
        String etudiantId = jwt.getSubject();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        return rdvRepository.findUpcomingRdvsByEtudiant(etudiantId, today)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Get past RDVs
    public List<RdvResponse> getPastRdvs(Jwt jwt) {
        String etudiantId = jwt.getSubject();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        return rdvRepository.findPastRdvsByEtudiant(etudiantId, today)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Book a new RDV
    @Transactional
    public RdvResponse bookRdv(Jwt jwt, RdvRequest request) {
        String etudiantId = jwt.getSubject();

        // Validate request
        validateRdvRequest(request);

        // Get student and doctor
        User etudiant = userRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        User medecin = userRepository.findById(request.getMedecinId())
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        // Check if doctor is available at this time
        boolean alreadyTaken = rdvRepository.existsByMedecin_IdAndDateAndHeure(
                medecin.getId(),
                request.getDate(),
                request.getHeure()
        );

        if (alreadyTaken) {
            throw new RuntimeException("Ce créneau est déjà pris");
        }

        // Check if student already has an RDV at this time
        boolean studentHasRdv = rdvRepository.existsByEtudiant_IdAndDateAndHeure(
                etudiantId,
                request.getDate(),
                request.getHeure()
        );

        if (studentHasRdv) {
            throw new RuntimeException("Vous avez déjà un rendez-vous à cette heure");
        }

        // Check if date is not in the past
        LocalDate rdvDate = LocalDate.parse(request.getDate());
        if (rdvDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Impossible de prendre un rendez-vous dans le passé");
        }

        // Create RDV
        Rdv rdv = new Rdv();
        rdv.setId(UUID.randomUUID().toString());
        rdv.setMedecin(medecin);
        rdv.setEtudiant(etudiant);
        rdv.setDate(request.getDate());
        rdv.setHeure(request.getHeure());
        rdv.setStatus(RdvStatus.CONFIRMED);

        Rdv saved = rdvRepository.save(rdv);
        return toResponse(saved);
    }

    // Cancel an RDV
    @Transactional
    public RdvResponse cancelRdv(Jwt jwt, String rdvId) {
        String etudiantId = jwt.getSubject();

        Rdv rdv = rdvRepository.findById(rdvId)
                .filter(r -> etudiantId.equals(r.getEtudiant().getId()))
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé ou ne vous appartient pas"));

        // Check if RDV is already canceled
        if (rdv.getStatus() == RdvStatus.CANCELED) {
            throw new RuntimeException("Ce rendez-vous est déjà annulé");
        }

        // Check if RDV date is in the past
        LocalDate rdvDate = LocalDate.parse(rdv.getDate());
        if (rdvDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Impossible d'annuler un rendez-vous passé");
        }

        rdv.setStatus(RdvStatus.CANCELED);
        Rdv updated = rdvRepository.save(rdv);
        return toResponse(updated);
    }

    // Update RDV (reschedule)
    @Transactional
    public RdvResponse updateRdv(Jwt jwt, String rdvId, RdvUpdateRequest request) {
        String etudiantId = jwt.getSubject();

        Rdv rdv = rdvRepository.findById(rdvId)
                .filter(r -> etudiantId.equals(r.getEtudiant().getId()))
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé ou ne vous appartient pas"));

        // Check if RDV can be modified
        if (rdv.getStatus() == RdvStatus.CANCELED) {
            throw new RuntimeException("Impossible de modifier un rendez-vous annulé");
        }

        LocalDate currentRdvDate = LocalDate.parse(rdv.getDate());
        if (currentRdvDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Impossible de modifier un rendez-vous passé");
        }

        boolean updated = false;

        // Update date if provided
        if (request.getDate() != null && !request.getDate().isEmpty()) {
            LocalDate newDate = LocalDate.parse(request.getDate());
            if (newDate.isBefore(LocalDate.now())) {
                throw new RuntimeException("La nouvelle date ne peut pas être dans le passé");
            }
            rdv.setDate(request.getDate());
            updated = true;
        }

        // Update time if provided
        if (request.getHeure() != null && !request.getHeure().isEmpty()) {
            rdv.setHeure(request.getHeure());
            updated = true;
        }

        // Update status if provided (only CONFIRMED or CANCELED allowed)
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                RdvStatus newStatus = RdvStatus.valueOf(request.getStatus());
                if (newStatus != RdvStatus.CONFIRMED && newStatus != RdvStatus.CANCELED) {
                    throw new RuntimeException("Statut invalide. Utilisez CONFIRMED ou CANCELED");
                }
                rdv.setStatus(newStatus);
                updated = true;
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Statut invalide. Utilisez CONFIRMED ou CANCELED");
            }
        }

        if (!updated) {
            throw new RuntimeException("Aucune modification fournie");
        }

        // Check for conflicts if date or time changed
        if (request.getDate() != null || request.getHeure() != null) {
            boolean conflict = rdvRepository.existsByMedecin_IdAndDateAndHeureAndIdNot(
                    rdv.getMedecin().getId(),
                    rdv.getDate(),
                    rdv.getHeure(),
                    rdv.getId()
            );

            if (conflict) {
                throw new RuntimeException("Ce créneau n'est plus disponible");
            }
        }

        Rdv updatedRdv = rdvRepository.save(rdv);
        return toResponse(updatedRdv);
    }

    // Delete RDV (only if canceled)
    @Transactional
    public void deleteRdv(Jwt jwt, String rdvId) {
        String etudiantId = jwt.getSubject();

        Rdv rdv = rdvRepository.findById(rdvId)
                .filter(r -> etudiantId.equals(r.getEtudiant().getId()))
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé ou ne vous appartient pas"));

        // Only allow deletion of canceled RDVs
        if (rdv.getStatus() != RdvStatus.CANCELED) {
            throw new RuntimeException("Seuls les rendez-vous annulés peuvent être supprimés");
        }

        rdvRepository.delete(rdv);
    }

    // Helper methods
    private void validateRdvRequest(RdvRequest request) {
        if (request.getMedecinId() == null || request.getMedecinId().trim().isEmpty()) {
            throw new IllegalArgumentException("L'ID du médecin est requis");
        }
        if (request.getDate() == null || request.getDate().trim().isEmpty()) {
            throw new IllegalArgumentException("La date est requise");
        }
        if (request.getHeure() == null || request.getHeure().trim().isEmpty()) {
            throw new IllegalArgumentException("L'heure est requise");
        }

        // Validate date format (YYYY-MM-DD)
        try {
            LocalDate.parse(request.getDate());
        } catch (Exception e) {
            throw new IllegalArgumentException("Format de date invalide. Utilisez YYYY-MM-DD");
        }

        // Validate time format (HH:MM)
        try {
            LocalTime.parse(request.getHeure());
        } catch (Exception e) {
            throw new IllegalArgumentException("Format d'heure invalide. Utilisez HH:MM");
        }
    }

    private List<UniversiteResponse> getUniversitesResponse(User user) {
        if (user.getUniversites() != null && !user.getUniversites().isEmpty()) {
            return user.getUniversites().stream()
                    .map(uni -> new UniversiteResponse(
                            uni.getId(),
                            uni.getNom(),
                            uni.getVille(),
                            uni.getAdresse(),
                            uni.getTelephone(),
                            uni.getNbEtudiants(),
                            uni.getHoraire(),
                            uni.getLogoPath(),
                            uni.getCode()
                    ))
                    .collect(Collectors.toList());
        }
        return List.of(); // Return empty list if no universities
    }

    private UniversiteResponse getUniversiteResponse(User user) {
        if (user.getUniversites() != null && !user.getUniversites().isEmpty()) {
            // Get the first university for backward compatibility
            Universite uni = user.getUniversites().iterator().next();
            return new UniversiteResponse(
                    uni.getId(),
                    uni.getNom(),
                    uni.getVille(),
                    uni.getAdresse(),
                    uni.getTelephone(),
                    uni.getNbEtudiants(),
                    uni.getHoraire(),
                    uni.getLogoPath(),
                    uni.getCode()
            );
        }
        return null;
    }

    private MedecinResponse toMedecinResponse(User medecin) {
        // For now, we don't have creneaux and rdvs in this context
        // You might want to fetch them if needed
        return new MedecinResponse(
                medecin.getId(),
                medecin.getNom(),
                medecin.getPrenom(),
                medecin.getEmail(),
                medecin.getPhotoPath(),
                medecin.getTelephone(),
                getUniversitesResponse(medecin), // Now returns List<UniversiteResponse>
                List.of(), // Empty list for creneaux
                List.of()  // Empty list for rdvs
        );
    }

    private EtudiantResponse toEtudiantResponse(User etudiant) {
        return new EtudiantResponse(
                etudiant.getId(),
                etudiant.getNom(),
                etudiant.getPrenom(),
                etudiant.getEmail(),
                etudiant.getTelephone(),
                etudiant.getPhotoPath(),
                getUniversiteResponse(etudiant) // Single university for student
        );
    }

    private RdvResponse toResponse(Rdv rdv) {
        MedecinResponse medecinResponse = toMedecinResponse(rdv.getMedecin());
        EtudiantResponse etudiantResponse = toEtudiantResponse(rdv.getEtudiant());

        return new RdvResponse(
                rdv.getId(),
                rdv.getDate(),
                rdv.getHeure(),
                rdv.getStatus().toString(),
                medecinResponse,
                etudiantResponse
        );
    }
}