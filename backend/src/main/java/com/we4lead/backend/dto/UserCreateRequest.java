package com.we4lead.backend.dto;

import com.we4lead.backend.entity.Genre;
import com.we4lead.backend.entity.Situation;

public class UserCreateRequest {
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private String role;
    private Long universiteId;

    // Nouveaux champs
    private String specialite;
    private Genre genre;
    private Situation situation;
    private String niveauEtude;

    // Getters et Setters existants
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getUniversiteId() { return universiteId; }
    public void setUniversiteId(Long universiteId) { this.universiteId = universiteId; }

    // Getters et Setters pour les nouveaux champs
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public Situation getSituation() { return situation; }
    public void setSituation(Situation situation) { this.situation = situation; }

    public String getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(String niveauEtude) { this.niveauEtude = niveauEtude; }
}