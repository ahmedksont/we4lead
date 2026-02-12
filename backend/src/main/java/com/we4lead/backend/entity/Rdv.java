package com.we4lead.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rdvs")
public class Rdv {

    @Id
    private String id;

    private String date;
    private String heure;

    @ManyToOne
    @JoinColumn(name = "medecin_id")
    private User medecin;

    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private User etudiant;

    @Enumerated(EnumType.STRING)
    private RdvStatus status;

    public Rdv() {}

    public Rdv(String id, String date, String heure, User medecin, User etudiant) {
        this.id = id;
        this.date = date;
        this.heure = heure;
        this.medecin = medecin;
        this.etudiant = etudiant;
        this.status = RdvStatus.CONFIRMED; // Default status
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }

    public User getMedecin() { return medecin; }
    public void setMedecin(User medecin) { this.medecin = medecin; }

    public User getEtudiant() { return etudiant; }
    public void setEtudiant(User etudiant) { this.etudiant = etudiant; }

    public RdvStatus getStatus() { return status; }
    public void setStatus(RdvStatus status) { this.status = status; }
}