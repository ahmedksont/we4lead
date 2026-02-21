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
    private Genre genre;
    private Situation situation;
    private String niveauEtude;
    private long nombreDemandes; // NOUVEAU CHAMP

    // Constructeur avec tous les champs (y compris nombreDemandes)
    public EtudiantResponse(String id, String nom, String prenom, String email, String telephone,
                            String photoUrl, UniversiteResponse universite,
                            Genre genre, Situation situation, String niveauEtude,
                            long nombreDemandes) {
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
        this.nombreDemandes = nombreDemandes;
    }

    // Constructeur sans les nouveaux champs (pour la rétrocompatibilité)
    public EtudiantResponse(String id, String nom, String prenom, String email, String telephone,
                            String photoUrl, UniversiteResponse universite) {
        this(id, nom, prenom, email, telephone, photoUrl, universite, null, null, null, 0);
    }

    // Constructeur avec les anciens nouveaux champs mais sans nombreDemandes
    public EtudiantResponse(String id, String nom, String prenom, String email, String telephone,
                            String photoUrl, UniversiteResponse universite,
                            Genre genre, Situation situation, String niveauEtude) {
        this(id, nom, prenom, email, telephone, photoUrl, universite, genre, situation, niveauEtude, 0);
    }

    // Getters existants
    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getPhotoUrl() { return photoUrl; }
    public UniversiteResponse getUniversite() { return universite; }
    public Genre getGenre() { return genre; }
    public Situation getSituation() { return situation; }
    public String getNiveauEtude() { return niveauEtude; }

    // NOUVEAU getter
    public long getNombreDemandes() { return nombreDemandes; }
}