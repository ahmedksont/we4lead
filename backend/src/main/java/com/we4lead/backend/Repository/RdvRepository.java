package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Rdv;
import com.we4lead.backend.entity.RdvStatus;
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

    long countByEtudiant_Id(String etudiantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Rdv r WHERE r.medecin.id = :medecinId")
    void deleteByMedecin_Id(@Param("medecinId") String medecinId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Rdv r WHERE r.etudiant.id = :etudiantId")
    void deleteByEtudiant_Id(@Param("etudiantId") String etudiantId);

    // FIXED: Use JOIN to access university ID directly
    @Query("SELECT r FROM Rdv r JOIN r.medecin.universites u WHERE u.id = :universiteId")
    List<Rdv> findByUniversiteId(@Param("universiteId") Long universiteId);

    @Query("SELECT r FROM Rdv r WHERE r.status = :status")
    List<Rdv> findByStatus(@Param("status") RdvStatus status);
}