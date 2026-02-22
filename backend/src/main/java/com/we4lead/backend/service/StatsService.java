package com.we4lead.backend.service;

import com.we4lead.backend.Repository.DemandeRepository;
import com.we4lead.backend.Repository.UniversiteRepository;
import com.we4lead.backend.Repository.UserRepository;
import com.we4lead.backend.dto.AdminStatsResponse;
import com.we4lead.backend.dto.DemandeResponse;
import com.we4lead.backend.entity.Demande;
import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.TypeSituation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final UserRepository userRepository;
    private final UniversiteRepository universiteRepository;
    private final DemandeRepository demandeRepository;
    private final DemandeService demandeService;

    public StatsService(
            UserRepository userRepository,
            UniversiteRepository universiteRepository,
            DemandeRepository demandeRepository,
            DemandeService demandeService) {
        this.userRepository = userRepository;
        this.universiteRepository = universiteRepository;
        this.demandeRepository = demandeRepository;
        this.demandeService = demandeService;
    }

    public AdminStatsResponse getAdminStats() {
        // Compter les différents éléments
        long nombreDemandes = demandeRepository.count();
        long nombreEtudiants = userRepository.countByRole(Role.ETUDIANT);
        long nombreMedecins = userRepository.countByRole(Role.MEDECIN);
        long nombreUniversites = universiteRepository.count();

        // Récupérer les 5 dernières demandes
        List<Demande> dernieresDemandes = demandeRepository.findTop5ByOrderByDateCreationDesc();
        List<DemandeResponse> dernieresDemandesResponse = dernieresDemandes.stream()
                .map(demandeService::mapToResponse)
                .collect(Collectors.toList());

        // Statistiques par type de situation
        Map<String, Long> demandesParType = new HashMap<>();
        List<Object[]> typeStats = demandeRepository.countDemandesByType();
        for (Object[] stat : typeStats) {
            TypeSituation type = (TypeSituation) stat[0];
            Long count = (Long) stat[1];
            demandesParType.put(type.toString(), count);
        }

        // Statistiques par mois
        Map<String, Long> demandesParMois = new HashMap<>();
        List<Object[]> monthStats = demandeRepository.countDemandesByMonth();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Object[] stat : monthStats) {
            Integer year = (Integer) stat[0];
            Integer month = (Integer) stat[1];
            Long count = (Long) stat[2];

            String monthKey = String.format("%d-%02d", year, month);
            demandesParMois.put(monthKey, count);
        }

        return new AdminStatsResponse(
                nombreDemandes,
                nombreEtudiants,
                nombreMedecins,
                nombreUniversites,
                dernieresDemandesResponse,
                demandesParType,
                demandesParMois
        );
    }

    // Stats pour un admin d'université spécifique
    public AdminStatsResponse getUniversityAdminStats(Long universiteId) {
        // Compter les différents éléments pour une université spécifique
        long nombreDemandes = demandeRepository.countByUniversiteId(universiteId);
        long nombreEtudiants = userRepository.countByRoleAndUniversiteId(Role.ETUDIANT, universiteId);
        long nombreMedecins = userRepository.countByRoleAndUniversiteId(Role.MEDECIN, universiteId);
        long nombreUniversites = 1; // Pour un admin d'université, il n'y a qu'une seule université

        // Récupérer les 5 dernières demandes pour cette université
        List<Demande> dernieresDemandes = demandeRepository.findTop5ByUniversiteIdOrderByDateCreationDesc(universiteId);
        List<DemandeResponse> dernieresDemandesResponse = dernieresDemandes.stream()
                .map(demandeService::mapToResponse)
                .collect(Collectors.toList());

        // Statistiques par type pour cette université
        Map<String, Long> demandesParType = new HashMap<>();
        List<Object[]> typeStats = demandeRepository.countDemandesByTypeForUniversite(universiteId);
        for (Object[] stat : typeStats) {
            TypeSituation type = (TypeSituation) stat[0];
            Long count = (Long) stat[1];
            demandesParType.put(type.toString(), count);
        }

        return new AdminStatsResponse(
                nombreDemandes,
                nombreEtudiants,
                nombreMedecins,
                nombreUniversites,
                dernieresDemandesResponse,
                demandesParType,
                new HashMap<>() // Pas de stats par mois pour simplifier
        );
    }
}