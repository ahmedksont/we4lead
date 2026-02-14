package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Creneau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CreneauRepository extends JpaRepository<Creneau, String> {

    List<Creneau> findByMedecin_Id(String medecinId);

    @Query("SELECT c FROM Creneau c WHERE c.medecin.id = :medecinId AND c.jour = :jour")
    List<Creneau> findByMedecinIdAndJour(
            @Param("medecinId") String medecinId,
            @Param("jour") String jour);

    boolean existsByMedecin_IdAndJourAndDebutAndFin(
            String medecinId, String jour, String debut, String fin);

    // Overlap check (excluding a specific slot when updating)
    @Query("""
        SELECT COUNT(c) > 0 FROM Creneau c
        WHERE c.medecin.id = :medecinId
          AND c.jour = :jour
          AND c.id != :excludeId
          AND (
              (c.debut <= :fin AND c.fin >= :debut)
          )
        """)
    boolean existsOverlapping(
            @Param("medecinId") String medecinId,
            @Param("jour") String jour,
            @Param("debut") String debut,
            @Param("fin") String fin,
            @Param("excludeId") String excludeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Creneau c WHERE c.medecin.id = :medecinId")
    void deleteByMedecin_Id(@Param("medecinId") String medecinId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Creneau c WHERE c.id = :id AND c.medecin.id = :medecinId")
    void deleteByIdAndMedecinId(
            @Param("id") String id,
            @Param("medecinId") String medecinId);
}