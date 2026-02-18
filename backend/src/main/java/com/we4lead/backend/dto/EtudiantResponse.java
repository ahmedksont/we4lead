package com.we4lead.backend.dto;

import com.we4lead.backend.entity.Genre;
import com.we4lead.backend.entity.Situation;

public class EtudiantResponse {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String photoUrl;
    private UniversiteResponse universite;

    // Nouveaux champs
    private Genre genre;
    private Situation situation;
    private String niveauEtude;

    // Constructeur avec tous les champs
    public EtudiantResponse(String id, String nom, String prenom, String email, String telephone,
                            String photoUrl, UniversiteResponse universite,
                            Genre genre, Situation situation, String niveauEtude) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.photoUrl = photoUrl;
        this.universite = universite;
        this.genre = genre;
        this.situation = situation;
        this.niveauEtude = niveauEtude;
    }

    // Constructeur sans les nouveaux champs (pour la rétrocompatibilité si nécessaire)
    public EtudiantResponse(String id, String nom, String prenom, String email, String telephone,
                            String photoUrl, UniversiteResponse universite) {
        this(id, nom, prenom, email, telephone, photoUrl, universite, null, null, null);
    }

    // Getters
    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getPhotoUrl() { return photoUrl; }
    public UniversiteResponse getUniversite() { return universite; }

    // Getters pour les nouveaux champs
    public Genre getGenre() { return genre; }
    public Situation getSituation() { return situation; }
    public String getNiveauEtude() { return niveauEtude; }
}