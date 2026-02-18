package com.we4lead.backend.dto;

import com.we4lead.backend.entity.Genre;
import com.we4lead.backend.entity.Situation;

public class UserUpdateRequest {

    private String nom;
    private String prenom;
    private String telephone;

    // Nouveaux champs
    private String specialite;
    private Genre genre;
    private Situation situation;
    private String niveauEtude;

    public UserUpdateRequest() {}

    // Constructeur avec tous les champs
    public UserUpdateRequest(String nom, String prenom, String telephone,
                             String specialite, Genre genre, Situation situation,
                             String niveauEtude) {
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.specialite = specialite;
        this.genre = genre;
        this.situation = situation;
        this.niveauEtude = niveauEtude;
    }

    // Getters et Setters existants
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

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