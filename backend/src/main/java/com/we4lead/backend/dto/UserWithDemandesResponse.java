package com.we4lead.backend.dto;

import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.Genre;
import com.we4lead.backend.entity.Situation;
import java.util.List;

public class UserWithDemandesResponse {
    private String id;
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private Role role;
    private String photoUrl;
    private String specialite;
    private Genre genre;
    private Situation situation;
    private String niveauEtude;
    private UniversiteResponse universite;
    private List<UniversiteResponse> universites;
    private long nombreDemandes; // Nouveau champ

    public UserWithDemandesResponse(String id, String email, String nom, String prenom,
                                    String telephone, Role role, String photoUrl,
                                    String specialite, Genre genre, Situation situation,
                                    String niveauEtude, UniversiteResponse universite,
                                    List<UniversiteResponse> universites, long nombreDemandes) {
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.role = role;
        this.photoUrl = photoUrl;
        this.specialite = specialite;
        this.genre = genre;
        this.situation = situation;
        this.niveauEtude = niveauEtude;
        this.universite = universite;
        this.universites = universites;
        this.nombreDemandes = nombreDemandes;
    }

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getTelephone() { return telephone; }
    public Role getRole() { return role; }
    public String getPhotoUrl() { return photoUrl; }
    public String getSpecialite() { return specialite; }
    public Genre getGenre() { return genre; }
    public Situation getSituation() { return situation; }
    public String getNiveauEtude() { return niveauEtude; }
    public UniversiteResponse getUniversite() { return universite; }
    public List<UniversiteResponse> getUniversites() { return universites; }
    public long getNombreDemandes() { return nombreDemandes; }
}