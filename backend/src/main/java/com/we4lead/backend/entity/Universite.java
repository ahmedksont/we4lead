package com.we4lead.backend.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "universite")
public class Universite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String ville;

    @Column(unique = true)
    private String code;

    private String adresse;
    private String telephone;
    private Integer nbEtudiants;
    private String horaire;
    private String logoPath; // store file path

    // ====== Link to medecins ======
    @ManyToMany(mappedBy = "universites")
    private Set<User> medecins = new HashSet<>();


    public Universite() {}

    public Universite(String nom, String ville) {
        this.nom = nom;
        this.ville = ville;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Integer getNbEtudiants() { return nbEtudiants; }
    public void setNbEtudiants(Integer nbEtudiants) { this.nbEtudiants = nbEtudiants; }

    public String getHoraire() { return horaire; }
    public void setHoraire(String horaire) { this.horaire = horaire; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public Set<User> getMedecins() { return medecins; }
    public void setMedecins(Set<User> medecins) { this.medecins = medecins; }
}
