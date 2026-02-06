package com.we4lead.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "creneaux")
public class Creneau {

    @Id
    private String id;

    private String jour;
    private String debut;
    private String fin;

    @ManyToOne
    @JoinColumn(name = "medecin_id")
    private User medecin;
    public Creneau() {}

    public Creneau(String id, String jour, String debut, String fin, User medecin) {
        this.id = id;
        this.jour = jour;
        this.debut = debut;
        this.fin = fin;
        this.medecin = medecin;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }

    public String getDebut() { return debut; }
    public void setDebut(String debut) { this.debut = debut; }

    public String getFin() { return fin; }
    public void setFin(String fin) { this.fin = fin; }

    public User getMedecin() { return medecin; }
    public void setMedecin(User medecin) { this.medecin = medecin; }
}
