package com.we4lead.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "demandes")
public class Demande {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeSituation typeSituation;

    @Column(nullable = false, length = 2000)
    private String description;

    private String lieuPrincipal;

    @Column(nullable = false)
    private String periode; // Format: "2024-2025" ou "Septembre 2024 - Janvier 2025"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id")
    private User medecin; // Médecin concerné par la demande (optionnel)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private User etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "universite_id", nullable = false)
    private Universite universite;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    // Constructeurs
    public Demande() {
        this.id = java.util.UUID.randomUUID().toString();
        this.dateCreation = LocalDateTime.now();
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public TypeSituation getTypeSituation() { return typeSituation; }
    public void setTypeSituation(TypeSituation typeSituation) { this.typeSituation = typeSituation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLieuPrincipal() { return lieuPrincipal; }
    public void setLieuPrincipal(String lieuPrincipal) { this.lieuPrincipal = lieuPrincipal; }

    public String getPeriode() { return periode; }
    public void setPeriode(String periode) { this.periode = periode; }

    public User getMedecin() { return medecin; }
    public void setMedecin(User medecin) { this.medecin = medecin; }

    public User getEtudiant() { return etudiant; }
    public void setEtudiant(User etudiant) { this.etudiant = etudiant; }

    public Universite getUniversite() { return universite; }
    public void setUniversite(Universite universite) { this.universite = universite; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}