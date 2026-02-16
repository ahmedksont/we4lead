package com.we4lead.backend.dto;

import java.util.List;

public class UniversiteWithMedecinsResponse {
    private Long id;
    private String nom;
    private String ville;
    private String adresse;
    private String telephone;
    private Integer nbEtudiants;
    private String horaire;
    private String logoPath;
    private String code;
    private List<MedecinResponse> medecins;

    public UniversiteWithMedecinsResponse(Long id, String nom, String ville,
                                          String adresse, String telephone,
                                          Integer nbEtudiants, String horaire,
                                          String logoPath, String code,
                                          List<MedecinResponse> medecins) {
        this.id = id;
        this.nom = nom;
        this.ville = ville;
        this.adresse = adresse;
        this.telephone = telephone;
        this.nbEtudiants = nbEtudiants;
        this.horaire = horaire;
        this.logoPath = logoPath;
        this.code = code;
        this.medecins = medecins;
    }

    // Getters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getVille() { return ville; }
    public String getAdresse() { return adresse; }
    public String getTelephone() { return telephone; }
    public Integer getNbEtudiants() { return nbEtudiants; }
    public String getHoraire() { return horaire; }
    public String getLogoPath() { return logoPath; }
    public String getCode() { return code; }
    public List<MedecinResponse> getMedecins() { return medecins; }
}