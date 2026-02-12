package com.we4lead.backend.dto;

public class RdvResponse {
    private String id;
    private String date;
    private String heure;
    private String status;
    private MedecinResponse medecin;
    private EtudiantResponse etudiant;

    public RdvResponse(String id, String date, String heure, String status,
                       MedecinResponse medecin, EtudiantResponse etudiant) {
        this.id = id;
        this.date = date;
        this.heure = heure;
        this.status = status;
        this.medecin = medecin;
        this.etudiant = etudiant;
    }

    // Getters
    public String getId() { return id; }
    public String getDate() { return date; }
    public String getHeure() { return heure; }
    public String getStatus() { return status; }
    public MedecinResponse getMedecin() { return medecin; }
    public EtudiantResponse getEtudiant() { return etudiant; }
}