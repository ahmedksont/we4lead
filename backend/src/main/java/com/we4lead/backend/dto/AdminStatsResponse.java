package com.we4lead.backend.dto;

import java.util.List;
import java.util.Map;

public class AdminStatsResponse {
    private long nombreDemandes;
    private long nombreEtudiants;
    private long nombreMedecins;
    private long nombreUniversites;
    private List<DemandeResponse> dernieresDemandes;
    private Map<String, Long> demandesParType;
    private Map<String, Long> demandesParMois;

    // Constructeurs
    public AdminStatsResponse() {}

    public AdminStatsResponse(long nombreDemandes, long nombreEtudiants, long nombreMedecins,
                              long nombreUniversites, List<DemandeResponse> dernieresDemandes,
                              Map<String, Long> demandesParType, Map<String, Long> demandesParMois) {
        this.nombreDemandes = nombreDemandes;
        this.nombreEtudiants = nombreEtudiants;
        this.nombreMedecins = nombreMedecins;
        this.nombreUniversites = nombreUniversites;
        this.dernieresDemandes = dernieresDemandes;
        this.demandesParType = demandesParType;
        this.demandesParMois = demandesParMois;
    }

    // Getters et Setters
    public long getNombreDemandes() { return nombreDemandes; }
    public void setNombreDemandes(long nombreDemandes) { this.nombreDemandes = nombreDemandes; }

    public long getNombreEtudiants() { return nombreEtudiants; }
    public void setNombreEtudiants(long nombreEtudiants) { this.nombreEtudiants = nombreEtudiants; }

    public long getNombreMedecins() { return nombreMedecins; }
    public void setNombreMedecins(long nombreMedecins) { this.nombreMedecins = nombreMedecins; }

    public long getNombreUniversites() { return nombreUniversites; }
    public void setNombreUniversites(long nombreUniversites) { this.nombreUniversites = nombreUniversites; }

    public List<DemandeResponse> getDernieresDemandes() { return dernieresDemandes; }
    public void setDernieresDemandes(List<DemandeResponse> dernieresDemandes) { this.dernieresDemandes = dernieresDemandes; }

    public Map<String, Long> getDemandesParType() { return demandesParType; }
    public void setDemandesParType(Map<String, Long> demandesParType) { this.demandesParType = demandesParType; }

    public Map<String, Long> getDemandesParMois() { return demandesParMois; }
    public void setDemandesParMois(Map<String, Long> demandesParMois) { this.demandesParMois = demandesParMois; }
}