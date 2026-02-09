package com.we4lead.backend.dto;

import java.util.List;

public class MedecinResponse {

    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String photoUrl;
    private List<CreneauResponse> creneaux;

    public MedecinResponse(
            String id,
            String nom,
            String prenom,
            String email,
            String photoUrl,
            List<CreneauResponse> creneaux
    ) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.photoUrl = photoUrl;
        this.creneaux = creneaux;
    }

    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }
    public List<CreneauResponse> getCreneaux() { return creneaux; }
}
