package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Rdv;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RdvRepository extends JpaRepository<Rdv, String> {

    boolean existsByMedecin_IdAndDateAndHeure(String medecinId, String date, String heure);
    List<Rdv> findByMedecin_Id(String medecinId);

    List<Rdv> findByEtudiant_Id(String etudiantId);
    long countByMedecin_Id(String medecinId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Rdv r WHERE r.medecin.id = :medecinId")
    void deleteByMedecin_Id(@Param("medecinId") String medecinId);
}
