package com.we4lead.backend.dto;

public class CreneauResponse {

    private String id;
    private String jour;
    private String debut;
    private String fin;

    public CreneauResponse(String id, String jour, String debut, String fin) {
        this.id = id;
        this.jour = jour;
        this.debut = debut;
        this.fin = fin;
    }

    public String getId() { return id; }
    public String getJour() { return jour; }
    public String getDebut() { return debut; }
    public String getFin() { return fin; }
}
