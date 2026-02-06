package com.we4lead.backend.dto;

public class UserUpdateRequest {

    private String nom;
    private String prenom;
    private String telephone;

    public UserUpdateRequest() {}

    public UserUpdateRequest(String nom, String prenom, String telephone) {
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
}
