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

    // Informations sur le médecin (si présent)
    private String medecinId;
    private String medecinNom;
    private String medecinPrenom;
    private String medecinSpecialite;
    private String medecinEmail;

    // Informations sur l'étudiant
    private String etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantEmail;
    private String etudiantTelephone;
    private String etudiantNiveauEtude;

    // Informations sur l'université
    private Long universiteId;
    private String universiteNom;

    public DemandeResponse(String id, TypeSituation typeSituation, String description,
                           String lieuPrincipal, String periode, LocalDateTime dateCreation,
                           String medecinId, String medecinNom, String medecinPrenom,
                           String medecinSpecialite, String medecinEmail,
                           String etudiantId, String etudiantNom, String etudiantPrenom,
                           String etudiantEmail, String etudiantTelephone, String etudiantNiveauEtude,
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
        this.etudiantId = etudiantId;
        this.etudiantNom = etudiantNom;
        this.etudiantPrenom = etudiantPrenom;
        this.etudiantEmail = etudiantEmail;
        this.etudiantTelephone = etudiantTelephone;
        this.etudiantNiveauEtude = etudiantNiveauEtude;
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
    public String getEtudiantId() { return etudiantId; }
    public String getEtudiantNom() { return etudiantNom; }
    public String getEtudiantPrenom() { return etudiantPrenom; }
    public String getEtudiantEmail() { return etudiantEmail; }
    public String getEtudiantTelephone() { return etudiantTelephone; }
    public String getEtudiantNiveauEtude() { return etudiantNiveauEtude; }
    public Long getUniversiteId() { return universiteId; }
    public String getUniversiteNom() { return universiteNom; }
}