package com.we4lead.backend.dto;

import com.we4lead.backend.entity.TypeSituation;

public class DemandeUpdateRequest {
    private TypeSituation typeSituation;
    private String lieuPrincipal;
    private String periode;
    private String medecinId;

    // Getters et Setters
    public TypeSituation getTypeSituation() { return typeSituation; }
    public void setTypeSituation(TypeSituation typeSituation) { this.typeSituation = typeSituation; }

    public String getLieuPrincipal() { return lieuPrincipal; }
    public void setLieuPrincipal(String lieuPrincipal) { this.lieuPrincipal = lieuPrincipal; }

    public String getPeriode() { return periode; }
    public void setPeriode(String periode) { this.periode = periode; }

    public String getMedecinId() { return medecinId; }
    public void setMedecinId(String medecinId) { this.medecinId = medecinId; }
}