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
     * Crée une demande avec création automatique de l'utilisateur s'il n'existe pas
     * et envoie un email au médecin concerné
     */
    @Transactional
    public DemandeResponse createDemandeWithUser(DemandeWithStudentRequest request) {
        // Valider les champs obligatoires
        validateRequest(request);

        // Récupérer l'université
        Universite universite = universiteRepository.findById(request.getUniversiteId())
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée avec l'ID: " + request.getUniversiteId()));

        // Récupérer ou créer l'utilisateur
        User user = getOrCreateUser(request, universite);

        // Vérifier que l'utilisateur est associé à cette université
        if (!user.getUniversites().contains(universite)) {
            user.getUniversites().add(universite);
            userRepository.save(user);
        }

        // Créer la demande
        Demande demande = new Demande();
        demande.setTypeSituation(request.getTypeSituation());
        demande.setDescription(request.getDescription());
        demande.setLieuPrincipal(request.getLieuPrincipal());
        demande.setPeriode(request.getPeriode());
        demande.setEtudiant(user); // L'utilisateur qui fait la demande
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

            demande.setMedecin(medecin);
        }

        Demande savedDemande = demandeRepository.save(demande);
        DemandeResponse response = mapToResponse(savedDemande);

        // Envoyer les emails
        sendEmails(response, user, medecin, universite, request.getUserType());

        return response;
    }

    /**
     * Récupère un utilisateur par email ou le crée s'il n'existe pas
     * selon le type d'utilisateur (ETUDIANT ou PROFESSEUR)
     */
    private User getOrCreateUser(DemandeWithStudentRequest request, Universite universite) {
        return userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(UUID.randomUUID().toString());
                    newUser.setEmail(request.getEmail());
                    newUser.setNom(request.getNom());
                    newUser.setPrenom(request.getPrenom());
                    newUser.setTelephone(request.getTelephone());

                    // Déterminer le rôle en fonction du userType
                    String userType = request.getUserType();
                    if ("PROFESSEUR".equalsIgnoreCase(userType)) {
                        newUser.setRole(Role.PROFESSEUR);
                    } else {
                        // Par défaut, ETUDIANT
                        newUser.setRole(Role.ETUDIANT);
                        newUser.setGenre(request.getGenre());
                        newUser.setSituation(request.getSituation());
                        newUser.setNiveauEtude(request.getNiveauEtude());
                    }

                    // Associer l'université à l'utilisateur
                    newUser.getUniversites().add(universite);
                    newUser.setUniversite(universite);

                    return userRepository.save(newUser);
                });
    }

    /**
     * Envoie les emails de notification
     */
    private void sendEmails(DemandeResponse demande, User user, User medecin, Universite universite, String userType) {
        // Déterminer le libellé du type d'utilisateur
        String userTypeLabel = "PROFESSEUR".equalsIgnoreCase(userType) ? "Professeur" : "Étudiant";

        // Formater les informations de l'utilisateur pour l'email du médecin
        String userInfo = String.format("""
            Type : %s
            Nom complet : %s %s
            Email : %s
            Téléphone : %s
            %s
            """,
                userTypeLabel,
                user.getPrenom(),
                user.getNom(),
                user.getEmail(),
                user.getTelephone() != null ? user.getTelephone() : "Non renseigné",
                "ETUDIANT".equalsIgnoreCase(userType) && user.getNiveauEtude() != null ?
                        "Niveau d'étude : " + user.getNiveauEtude() : ""
        );

        // Informations sur l'université
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
                emailService.sendDemandeToMedecin(medecin, demande, userInfo, universiteInfo);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de l'email au médecin: " + e.getMessage());
            }
        }

        // Envoyer un email de confirmation à l'utilisateur
        try {
            emailService.sendDemandeConfirmationToUser(user.getEmail(), demande, medecin, userTypeLabel);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email de confirmation: " + e.getMessage());
        }
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
            throw new IllegalArgumentException("L'email est obligatoire");
        }
        if (request.getNom() == null || request.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
        if (request.getPrenom() == null || request.getPrenom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est obligatoire");
        }
        if (request.getUniversiteId() == null) {
            throw new IllegalArgumentException("L'ID de l'université est obligatoire");
        }

        // Validation du userType
        String userType = request.getUserType();
        if (userType != null && !userType.isEmpty()) {
            if (!"ETUDIANT".equalsIgnoreCase(userType) && !"PROFESSEUR".equalsIgnoreCase(userType)) {
                throw new IllegalArgumentException("Le type d'utilisateur doit être 'ETUDIANT' ou 'PROFESSEUR'");
            }
        }
    }

    /**
     * Récupère toutes les demandes d'un utilisateur par son email
     */
    public List<DemandeResponse> getDemandesByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'email: " + email));

        return demandeRepository.findByEtudiantIdOrderByDateCreationDesc(user.getId())
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
    public DemandeResponse mapToResponse(Demande demande) {
        User user = demande.getEtudiant();
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
                user.getId(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getTelephone(),
                user.getNiveauEtude(),
                user.getRole() != null ? user.getRole().toString() : "ETUDIANT", // Ajout du rôle
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