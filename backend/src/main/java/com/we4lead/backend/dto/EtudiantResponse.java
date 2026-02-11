package com.we4lead.backend.dto;

public class EtudiantResponse {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String photoUrl;
    private UniversiteResponse universite;

    public EtudiantResponse(String id, String nom, String prenom, String email, String telephone,
                            String photoUrl, UniversiteResponse universite) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.photoUrl = photoUrl;
        this.universite = universite;
    }

    // Getters
    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getPhotoUrl() { return photoUrl; }
    public UniversiteResponse getUniversite() { return universite; }
}