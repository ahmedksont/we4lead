package com.we4lead.backend.dto;

import org.springframework.web.multipart.MultipartFile;

public class UniversiteRequest {
    private String nom;
    private String ville;
    private String adresse;
    private String telephone;
    private Integer nbEtudiants;
    private String horaire;
    private MultipartFile logo; // for file upload

    // Getters and setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Integer getNbEtudiants() { return nbEtudiants; }
    public void setNbEtudiants(Integer nbEtudiants) { this.nbEtudiants = nbEtudiants; }

    public String getHoraire() { return horaire; }
    public void setHoraire(String horaire) { this.horaire = horaire; }

    public MultipartFile getLogo() { return logo; }
    public void setLogo(MultipartFile logo) { this.logo = logo; }
}
