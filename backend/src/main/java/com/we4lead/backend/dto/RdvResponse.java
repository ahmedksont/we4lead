package com.we4lead.backend.dto;

public class RdvResponse {
    private String id;
    private String date;
    private String heure;
    private String etudiant;

    public RdvResponse(String id, String date, String heure, String etudiant) {
        this.id = id;
        this.date = date;
        this.heure = heure;
        this.etudiant = etudiant;
    }

    // getters
    public String getId() { return id; }
    public String getDate() { return date; }
    public String getHeure() { return heure; }
    public String getEtudiant() { return etudiant; }
}
