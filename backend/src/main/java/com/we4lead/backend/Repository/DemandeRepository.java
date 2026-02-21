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

    // Demandes par étudiant
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

    // Compter le nombre total de demandes pour un étudiant
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
    @Query("SELECT FUNCTION('MONTH', d.dateCreation), FUNCTION('YEAR', d.dateCreation), COUNT(d) " +
            "FROM Demande d WHERE d.etudiant.id = :etudiantId " +
            "GROUP BY FUNCTION('YEAR', d.dateCreation), FUNCTION('MONTH', d.dateCreation) " +
            "ORDER BY FUNCTION('YEAR', d.dateCreation) DESC, FUNCTION('MONTH', d.dateCreation) DESC")
    List<Object[]> countDemandesByMonthForEtudiant(@Param("etudiantId") String etudiantId);

    // Vérifier si un étudiant a des demandes
    boolean existsByEtudiantId(String etudiantId);
}