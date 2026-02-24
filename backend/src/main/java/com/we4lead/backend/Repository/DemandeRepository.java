package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Demande;
import com.we4lead.backend.entity.TypeSituation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, String> {

    // Demandes par étudiant (demandeur)
    List<Demande> findByEtudiantIdOrderByDateCreationDesc(String etudiantId);

    // Demandes par médecin
    List<Demande> findByMedecinIdOrderByDateCreationDesc(String medecinId);

    // Demandes par université
    List<Demande> findByUniversiteIdOrderByDateCreationDesc(Long universiteId);

    // Demandes par type de situation
    List<Demande> findByTypeSituationOrderByDateCreationDesc(TypeSituation typeSituation);

    // Demandes par période
    List<Demande> findByPeriodeContainingOrderByDateCreationDesc(String periode);

    // Demandes par médecin et université
    List<Demande> findByMedecinIdAndUniversiteIdOrderByDateCreationDesc(String medecinId, Long universiteId);

    // ========== MÉTHODES DE COMPTAGE ==========

    // Compter le nombre total de demandes pour un étudiant (demandeur)
    long countByEtudiantId(String etudiantId);

    // Compter le nombre de demandes pour un étudiant par type de situation
    long countByEtudiantIdAndTypeSituation(String etudiantId, TypeSituation typeSituation);

    // Compter le nombre de demandes pour un médecin
    long countByMedecinId(String medecinId);

    // Compter le nombre de demandes pour une université
    long countByUniversiteId(Long universiteId);

    // Compter le nombre de demandes par type de situation (global)
    long countByTypeSituation(TypeSituation typeSituation);

    // ========== STATISTIQUES AVANCÉES ==========

    // Obtenir le nombre de demandes par type pour un étudiant spécifique
    @Query("SELECT d.typeSituation, COUNT(d) FROM Demande d WHERE d.etudiant.id = :etudiantId GROUP BY d.typeSituation")
    List<Object[]> countDemandesByTypeForEtudiant(@Param("etudiantId") String etudiantId);

    // Obtenir le nombre de demandes par mois pour un étudiant
    @Query("SELECT EXTRACT(YEAR FROM d.dateCreation), EXTRACT(MONTH FROM d.dateCreation), COUNT(d) " +
            "FROM Demande d WHERE d.etudiant.id = :etudiantId " +
            "GROUP BY EXTRACT(YEAR FROM d.dateCreation), EXTRACT(MONTH FROM d.dateCreation) " +
            "ORDER BY EXTRACT(YEAR FROM d.dateCreation) DESC, EXTRACT(MONTH FROM d.dateCreation) DESC")
    List<Object[]> countDemandesByMonthForEtudiant(@Param("etudiantId") String etudiantId);

    // Vérifier si un étudiant a des demandes
    boolean existsByEtudiantId(String etudiantId);

    // Récupérer les 5 dernières demandes (global)
    List<Demande> findTop5ByOrderByDateCreationDesc();

    // Récupérer les 5 dernières demandes pour une université
    List<Demande> findTop5ByUniversiteIdOrderByDateCreationDesc(Long universiteId);

    // Compter les demandes par type (global)
    @Query("SELECT d.typeSituation, COUNT(d) FROM Demande d GROUP BY d.typeSituation")
    List<Object[]> countDemandesByType();

    // Compter les demandes par type pour une université
    @Query("SELECT d.typeSituation, COUNT(d) FROM Demande d WHERE d.universite.id = :universiteId GROUP BY d.typeSituation")
    List<Object[]> countDemandesByTypeForUniversite(@Param("universiteId") Long universiteId);

    // Compter les demandes par mois (global) - Version PostgreSQL
    @Query("SELECT EXTRACT(YEAR FROM d.dateCreation), EXTRACT(MONTH FROM d.dateCreation), COUNT(d) " +
            "FROM Demande d " +
            "GROUP BY EXTRACT(YEAR FROM d.dateCreation), EXTRACT(MONTH FROM d.dateCreation) " +
            "ORDER BY EXTRACT(YEAR FROM d.dateCreation) DESC, EXTRACT(MONTH FROM d.dateCreation) DESC")
    List<Object[]> countDemandesByMonth();

    // Compter les demandes totales
    long count();

    // ========== NOUVELLES MÉTHODES POUR LE COMPTAGE PAR UTILISATEUR ==========

    // Compter les demandes pour tous les utilisateurs (demandeurs)
    @Query("SELECT d.etudiant.id, COUNT(d) FROM Demande d GROUP BY d.etudiant.id")
    List<Object[]> countAllDemandesByUser();

    // Compter les demandes pour plusieurs utilisateurs (demandeurs)
    @Query("SELECT d.etudiant.id, COUNT(d) FROM Demande d WHERE d.etudiant.id IN :userIds GROUP BY d.etudiant.id")
    List<Object[]> countDemandesByUserIds(@Param("userIds") List<String> userIds);
}