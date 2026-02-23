package com.we4lead.backend.dto;

import com.we4lead.backend.entity.TypeSituation;
import java.time.LocalDateTime;

public class DemandeResponse {
    private String id;
    private TypeSituation typeSituation;
    private String description;
    private String lieuPrincipal;
    private String periode;
    private LocalDateTime dateCreation;

    // Informations sur le médecin
    private String medecinId;
    private String medecinNom;
    private String medecinPrenom;
    private String medecinSpecialite;
    private String medecinEmail;

    // Informations sur l'utilisateur (étudiant ou professeur)
    private String userId;
    private String userNom;
    private String userPrenom;
    private String userEmail;
    private String userTelephone;
    private String userNiveauEtude;
    private String userRole; // NOUVEAU: rôle de l'utilisateur

    // Informations sur l'université
    private Long universiteId;
    private String universiteNom;

    public DemandeResponse(String id, TypeSituation typeSituation, String description,
                           String lieuPrincipal, String periode, LocalDateTime dateCreation,
                           String medecinId, String medecinNom, String medecinPrenom,
                           String medecinSpecialite, String medecinEmail,
                           String userId, String userNom, String userPrenom,
                           String userEmail, String userTelephone, String userNiveauEtude,
                           String userRole, // NOUVEAU paramètre
                           Long universiteId, String universiteNom) {
        this.id = id;
        this.typeSituation = typeSituation;
        this.description = description;
        this.lieuPrincipal = lieuPrincipal;
        this.periode = periode;
        this.dateCreation = dateCreation;
        this.medecinId = medecinId;
        this.medecinNom = medecinNom;
        this.medecinPrenom = medecinPrenom;
        this.medecinSpecialite = medecinSpecialite;
        this.medecinEmail = medecinEmail;
        this.userId = userId;
        this.userNom = userNom;
        this.userPrenom = userPrenom;
        this.userEmail = userEmail;
        this.userTelephone = userTelephone;
        this.userNiveauEtude = userNiveauEtude;
        this.userRole = userRole; // NOUVEAU
        this.universiteId = universiteId;
        this.universiteNom = universiteNom;
    }

    // Getters
    public String getId() { return id; }
    public TypeSituation getTypeSituation() { return typeSituation; }
    public String getDescription() { return description; }
    public String getLieuPrincipal() { return lieuPrincipal; }
    public String getPeriode() { return periode; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public String getMedecinId() { return medecinId; }
    public String getMedecinNom() { return medecinNom; }
    public String getMedecinPrenom() { return medecinPrenom; }
    public String getMedecinSpecialite() { return medecinSpecialite; }
    public String getMedecinEmail() { return medecinEmail; }
    public String getUserId() { return userId; }
    public String getUserNom() { return userNom; }
    public String getUserPrenom() { return userPrenom; }
    public String getUserEmail() { return userEmail; }
    public String getUserTelephone() { return userTelephone; }
    public String getUserNiveauEtude() { return userNiveauEtude; }
    public String getUserRole() { return userRole; } // NOUVEAU getter
    public Long getUniversiteId() { return universiteId; }
    public String getUniversiteNom() { return universiteNom; }
}