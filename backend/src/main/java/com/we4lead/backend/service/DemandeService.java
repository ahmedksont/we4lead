package com.we4lead.backend.service;

import com.we4lead.backend.Repository.DemandeRepository;
import com.we4lead.backend.Repository.UniversiteRepository;
import com.we4lead.backend.Repository.UserRepository;
import com.we4lead.backend.dto.DemandeResponse;
import com.we4lead.backend.dto.DemandeUpdateRequest;
import com.we4lead.backend.dto.DemandeWithStudentRequest;
import com.we4lead.backend.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final UserRepository userRepository;
    private final UniversiteRepository universiteRepository;
    private final EmailService emailService;

    public DemandeService(DemandeRepository demandeRepository,
                          UserRepository userRepository,
                          UniversiteRepository universiteRepository,
                          EmailService emailService) {
        this.demandeRepository = demandeRepository;
        this.userRepository = userRepository;
        this.universiteRepository = universiteRepository;
        this.emailService = emailService;
    }

    /**
     * Crée une demande avec création automatique de l'étudiant s'il n'existe pas
     * et envoie un email au médecin concerné
     */
    @Transactional
    public DemandeResponse createDemandeWithStudent(DemandeWithStudentRequest request) {
        // Valider les champs obligatoires
        validateRequest(request);

        // Récupérer ou créer l'étudiant
        User etudiant = getOrCreateStudent(request);

        // Récupérer l'université
        Universite universite = universiteRepository.findById(request.getUniversiteId())
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée avec l'ID: " + request.getUniversiteId()));

        // Vérifier que l'étudiant est associé à cette université
        if (!etudiant.getUniversites().contains(universite)) {
            etudiant.getUniversites().add(universite);
            userRepository.save(etudiant);
        }

        // Créer la demande
        Demande demande = new Demande();
        demande.setTypeSituation(request.getTypeSituation());
        demande.setDescription(request.getDescription());
        demande.setLieuPrincipal(request.getLieuPrincipal());
        demande.setPeriode(request.getPeriode());
        demande.setEtudiant(etudiant);
        demande.setUniversite(universite);

        User medecin = null;
        // Ajouter le médecin si fourni
        if (request.getMedecinId() != null && !request.getMedecinId().trim().isEmpty()) {
            medecin = userRepository.findById(request.getMedecinId())
                    .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé avec l'ID: " + request.getMedecinId()));

            // Vérifier que c'est bien un médecin
            if (medecin.getRole() != Role.MEDECIN) {
                throw new IllegalArgumentException("L'utilisateur avec l'ID " + request.getMedecinId() + " n'est pas un médecin");
            }

            // SUPPRIMÉ : Vérification que le médecin appartient à la même université
            // Le médecin peut être de n'importe quelle université ou même sans université

            demande.setMedecin(medecin);
        }

        Demande savedDemande = demandeRepository.save(demande);
        DemandeResponse response = mapToResponse(savedDemande);

        // Envoyer les emails
        sendEmails(response, etudiant, medecin, universite);

        return response;
    }

    /**
     * Envoie les emails de notification
     */
    private void sendEmails(DemandeResponse demande, User etudiant, User medecin, Universite universite) {
        // Formater les informations de l'étudiant pour l'email du médecin
        String etudiantInfo = String.format("""
            Nom complet : %s %s
            Email : %s
            Téléphone : %s
            Niveau d'étude : %s
            """,
                etudiant.getPrenom(),
                etudiant.getNom(),
                etudiant.getEmail(),
                etudiant.getTelephone() != null ? etudiant.getTelephone() : "Non renseigné",
                etudiant.getNiveauEtude() != null ? etudiant.getNiveauEtude() : "Non renseigné"
        );

        // Informations sur l'université de l'étudiant
        String universiteInfo = String.format("""
            Université : %s
            Ville : %s
            """,
                universite.getNom(),
                universite.getVille() != null ? universite.getVille() : "Non spécifiée"
        );

        // Envoyer l'email au médecin si un médecin est associé
        if (medecin != null) {
            try {
                emailService.sendDemandeToMedecin(medecin, demande, etudiantInfo, universiteInfo);
            } catch (Exception e) {
                // Log l'erreur mais ne pas bloquer la création
                System.err.println("Erreur lors de l'envoi de l'email au médecin: " + e.getMessage());
            }
        }

        // Envoyer un email de confirmation à l'étudiant
        try {
            emailService.sendDemandeConfirmationToEtudiant(etudiant.getEmail(), demande, medecin);
        } catch (Exception e) {
            // Log l'erreur mais ne pas bloquer la création
            System.err.println("Erreur lors de l'envoi de l'email de confirmation: " + e.getMessage());
        }
    }

    /**
     * Récupère un étudiant par email ou le crée s'il n'existe pas
     */
    private User getOrCreateStudent(DemandeWithStudentRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    User newStudent = new User();
                    newStudent.setId(UUID.randomUUID().toString());
                    newStudent.setEmail(request.getEmail());
                    newStudent.setNom(request.getNom());
                    newStudent.setPrenom(request.getPrenom());
                    newStudent.setTelephone(request.getTelephone());
                    newStudent.setRole(Role.ETUDIANT);
                    newStudent.setGenre(request.getGenre());
                    newStudent.setSituation(request.getSituation());
                    newStudent.setNiveauEtude(request.getNiveauEtude());

                    return userRepository.save(newStudent);
                });
    }

    /**
     * Valide les champs obligatoires de la requête
     */
    private void validateRequest(DemandeWithStudentRequest request) {
        if (request.getTypeSituation() == null) {
            throw new IllegalArgumentException("Le type de situation est obligatoire");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        if (request.getPeriode() == null || request.getPeriode().trim().isEmpty()) {
            throw new IllegalArgumentException("La période est obligatoire");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email de l'étudiant est obligatoire");
        }
        if (request.getNom() == null || request.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'étudiant est obligatoire");
        }
        if (request.getPrenom() == null || request.getPrenom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom de l'étudiant est obligatoire");
        }
        if (request.getUniversiteId() == null) {
            throw new IllegalArgumentException("L'ID de l'université est obligatoire");
        }
    }

    /**
     * Récupère toutes les demandes d'un étudiant par son email
     */
    public List<DemandeResponse> getDemandesByStudentEmail(String email) {
        User etudiant = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé avec l'email: " + email));

        return demandeRepository.findByEtudiantIdOrderByDateCreationDesc(etudiant.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les demandes d'un médecin
     */
    public List<DemandeResponse> getDemandesByMedecinId(String medecinId) {
        User medecin = userRepository.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé avec l'ID: " + medecinId));

        if (medecin.getRole() != Role.MEDECIN) {
            throw new IllegalArgumentException("L'utilisateur avec l'ID " + medecinId + " n'est pas un médecin");
        }

        return demandeRepository.findByMedecinIdOrderByDateCreationDesc(medecinId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les demandes d'une université
     */
    public List<DemandeResponse> getDemandesByUniversiteId(Long universiteId) {
        if (!universiteRepository.existsById(universiteId)) {
            throw new IllegalArgumentException("Université non trouvée avec l'ID: " + universiteId);
        }

        return demandeRepository.findByUniversiteIdOrderByDateCreationDesc(universiteId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les demandes (pour admin)
     */
    public List<DemandeResponse> getAllDemandes() {
        return demandeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une demande par son ID
     */
    public DemandeResponse getDemandeById(String id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande non trouvée avec l'ID: " + id));
        return mapToResponse(demande);
    }

    /**
     * Supprime une demande
     */
    @Transactional
    public void deleteDemande(String id) {
        if (!demandeRepository.existsById(id)) {
            throw new IllegalArgumentException("Demande non trouvée avec l'ID: " + id);
        }
        demandeRepository.deleteById(id);
    }

    /**
     * Convertit une entité Demande en DTO DemandeResponse
     */
    private DemandeResponse mapToResponse(Demande demande) {
        User etudiant = demande.getEtudiant();
        User medecin = demande.getMedecin();
        Universite universite = demande.getUniversite();

        return new DemandeResponse(
                demande.getId(),
                demande.getTypeSituation(),
                demande.getDescription(),
                demande.getLieuPrincipal(),
                demande.getPeriode(),
                demande.getDateCreation(),
                medecin != null ? medecin.getId() : null,
                medecin != null ? medecin.getNom() : null,
                medecin != null ? medecin.getPrenom() : null,
                medecin != null ? medecin.getSpecialite() : null,
                medecin != null ? medecin.getEmail() : null,
                etudiant.getId(),
                etudiant.getNom(),
                etudiant.getPrenom(),
                etudiant.getEmail(),
                etudiant.getTelephone(),
                etudiant.getNiveauEtude(),
                universite.getId(),
                universite.getNom()
        );
    }

    /**
     * Récupère les demandes par type de situation
     */
    public List<DemandeResponse> getDemandesByTypeSituation(TypeSituation typeSituation) {
        return demandeRepository.findByTypeSituationOrderByDateCreationDesc(typeSituation)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les demandes par période
     */
    public List<DemandeResponse> getDemandesByPeriode(String periode) {
        return demandeRepository.findByPeriodeContainingOrderByDateCreationDesc(periode)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les demandes d'un médecin pour une université spécifique
     */
    public List<DemandeResponse> getDemandesByMedecinAndUniversite(String medecinId, Long universiteId) {
        return demandeRepository.findByMedecinIdAndUniversiteIdOrderByDateCreationDesc(medecinId, universiteId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour une demande existante (la description n'est pas modifiable)
     */
    @Transactional
    public DemandeResponse updateDemande(String id, DemandeUpdateRequest request) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande non trouvée avec l'ID: " + id));

        boolean updated = false;

        // Mise à jour du type de situation
        if (request.getTypeSituation() != null) {
            demande.setTypeSituation(request.getTypeSituation());
            updated = true;
        }

        // Mise à jour du lieu principal
        if (request.getLieuPrincipal() != null) {
            demande.setLieuPrincipal(request.getLieuPrincipal());
            updated = true;
        }

        // Mise à jour de la période
        if (request.getPeriode() != null && !request.getPeriode().trim().isEmpty()) {
            demande.setPeriode(request.getPeriode());
            updated = true;
        }

        // Mise à jour du médecin
        if (request.getMedecinId() != null && !request.getMedecinId().trim().isEmpty()) {
            User medecin = userRepository.findById(request.getMedecinId())
                    .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé avec l'ID: " + request.getMedecinId()));

            if (medecin.getRole() != Role.MEDECIN) {
                throw new IllegalArgumentException("L'utilisateur avec l'ID " + request.getMedecinId() + " n'est pas un médecin");
            }

            demande.setMedecin(medecin);
            updated = true;
        }

        if (!updated) {
            throw new IllegalArgumentException("Aucune modification fournie");
        }

        Demande updatedDemande = demandeRepository.save(demande);
        return mapToResponse(updatedDemande);
    }
}