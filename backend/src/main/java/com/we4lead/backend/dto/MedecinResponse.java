package com.we4lead.backend.dto;

import java.util.List;

public class MedecinResponse {

    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String photoUrl;
    private String telephone;
    private String specialite; // Ajout du champ specialite
    private List<UniversiteResponse> universites;
    private List<CreneauResponse> creneaux;
    private List<RdvResponse> rdvs;

    public MedecinResponse(
            String id,
            String nom,
            String prenom,
            String email,
            String photoUrl,
            String telephone,
            String specialite, // Ajout du paramètre specialite
            List<UniversiteResponse> universites,
            List<CreneauResponse> creneaux,
            List<RdvResponse> rdvs
    ) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.photoUrl = photoUrl;
        this.telephone = telephone;
        this.specialite = specialite; // Initialisation du champ specialite
        this.universites = universites;
        this.creneaux = creneaux;
        this.rdvs = rdvs;
    }

    // Getters
    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }
    public String getTelephone() { return telephone; }
    public String getSpecialite() { return specialite; } // Getter pour specialite
    public List<UniversiteResponse> getUniversites() { return universites; }
    public List<CreneauResponse> getCreneaux() { return creneaux; }
    public List<RdvResponse> getRdvs() { return rdvs; }
}