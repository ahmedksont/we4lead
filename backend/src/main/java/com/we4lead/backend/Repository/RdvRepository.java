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

    // Check if an RDV exists for a doctor at a specific date and time
    boolean existsByMedecin_IdAndDateAndHeure(String medecinId, String date, String heure);

    // Check if an RDV exists for a student at a specific date and time
    boolean existsByEtudiant_IdAndDateAndHeure(String etudiantId, String date, String heure);

    // Check for conflicts excluding a specific RDV (for updates)
    @Query("SELECT COUNT(r) > 0 FROM Rdv r WHERE r.medecin.id = :medecinId AND r.date = :date AND r.heure = :heure AND r.id != :excludeId")
    boolean existsByMedecin_IdAndDateAndHeureAndIdNot(
            @Param("medecinId") String medecinId,
            @Param("date") String date,
            @Param("heure") String heure,
            @Param("excludeId") String excludeId);

    // Find RDVs by doctor
    List<Rdv> findByMedecin_Id(String medecinId);

    // Find RDVs by student (ordered by date and time)
    List<Rdv> findByEtudiant_IdOrderByDateDescHeureDesc(String etudiantId);

    // Find RDVs by student ID
    List<Rdv> findByEtudiant_Id(String etudiantId);

    // Find RDVs by student and date
    List<Rdv> findByEtudiant_IdAndDateOrderByHeure(String etudiantId, String date);

    // Find upcoming RDVs for a student (future dates)
    @Query("SELECT r FROM Rdv r WHERE r.etudiant.id = :etudiantId AND r.date >= :today AND r.status = 'CONFIRMED' ORDER BY r.date ASC, r.heure ASC")
    List<Rdv> findUpcomingRdvsByEtudiant(@Param("etudiantId") String etudiantId, @Param("today") String today);

    // Find past RDVs for a student
    @Query("SELECT r FROM Rdv r WHERE r.etudiant.id = :etudiantId AND r.date < :today ORDER BY r.date DESC, r.heure DESC")
    List<Rdv> findPastRdvsByEtudiant(@Param("etudiantId") String etudiantId, @Param("today") String today);

    // Count methods
    long countByMedecin_Id(String medecinId);
    long countByEtudiant_Id(String etudiantId);

    // Delete methods
    @Modifying
    @Transactional
    @Query("DELETE FROM Rdv r WHERE r.medecin.id = :medecinId")
    void deleteByMedecin_Id(@Param("medecinId") String medecinId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Rdv r WHERE r.etudiant.id = :etudiantId")
    void deleteByEtudiant_Id(@Param("etudiantId") String etudiantId);

    // Find by university (using JOIN to access university ID directly)
    @Query("SELECT r FROM Rdv r JOIN r.medecin.universites u WHERE u.id = :universiteId")
    List<Rdv> findByUniversiteId(@Param("universiteId") Long universiteId);

    // Find by status
    @Query("SELECT r FROM Rdv r WHERE r.status = :status")
    List<Rdv> findByStatus(@Param("status") RdvStatus status);
    // Add this method to RdvRepository.java

    @Query("SELECT r FROM Rdv r WHERE r.medecin.id = :medecinId AND r.date >= :today AND r.status = 'CONFIRMED' ORDER BY r.date ASC, r.heure ASC")
    List<Rdv> findUpcomingRdvsByMedecin(@Param("medecinId") String medecinId, @Param("today") String today);
}