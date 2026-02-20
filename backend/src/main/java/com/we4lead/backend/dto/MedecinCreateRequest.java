package com.we4lead.backend.dto;

import org.springframework.web.multipart.MultipartFile;

public class MedecinCreateRequest {
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private String universiteIds;  // Reçoit la chaîne "[1,2,3]"
    private String specialite;
    private String genre;
    private String situation;
    private String niveauEtude;
    private MultipartFile photo;

    // Getters et Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getUniversiteIds() { return universiteIds; }
    public void setUniversiteIds(String universiteIds) { this.universiteIds = universiteIds; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getSituation() { return situation; }
    public void setSituation(String situation) { this.situation = situation; }

    public String getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(String niveauEtude) { this.niveauEtude = niveauEtude; }

    public MultipartFile getPhoto() { return photo; }
    public void setPhoto(MultipartFile photo) { this.photo = photo; }
}