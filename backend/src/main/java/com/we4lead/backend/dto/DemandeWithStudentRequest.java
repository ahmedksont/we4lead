package com.we4lead.backend.dto;

import com.we4lead.backend.entity.Genre;
import com.we4lead.backend.entity.Situation;
import com.we4lead.backend.entity.TypeSituation;

public class DemandeWithStudentRequest {
    // Informations de la demande
    private TypeSituation typeSituation;
    private String description;
    private String lieuPrincipal;
    private String periode;
    private String medecinId; // ID du médecin concerné (optionnel)

    // Informations de l'étudiant (obligatoires)
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private Genre genre;
    private Situation situation;
    private String niveauEtude;

    // Université (obligatoire)
    private Long universiteId;

    // Getters et Setters
    public TypeSituation getTypeSituation() { return typeSituation; }
    public void setTypeSituation(TypeSituation typeSituation) { this.typeSituation = typeSituation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLieuPrincipal() { return lieuPrincipal; }
    public void setLieuPrincipal(String lieuPrincipal) { this.lieuPrincipal = lieuPrincipal; }

    public String getPeriode() { return periode; }
    public void setPeriode(String periode) { this.periode = periode; }

    public String getMedecinId() { return medecinId; }
    public void setMedecinId(String medecinId) { this.medecinId = medecinId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public Situation getSituation() { return situation; }
    public void setSituation(Situation situation) { this.situation = situation; }

    public String getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(String niveauEtude) { this.niveauEtude = niveauEtude; }

    public Long getUniversiteId() { return universiteId; }
    public void setUniversiteId(Long universiteId) { this.universiteId = universiteId; }
}