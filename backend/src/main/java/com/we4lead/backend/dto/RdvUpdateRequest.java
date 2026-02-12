package com.we4lead.backend.dto;

public class RdvUpdateRequest {
    private String date;
    private String heure;
    private String status;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}