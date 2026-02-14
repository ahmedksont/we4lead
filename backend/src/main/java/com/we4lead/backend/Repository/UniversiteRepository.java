package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Universite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UniversiteRepository extends JpaRepository<Universite, Long> {
    Universite findByNom(String nom);

    @Query("SELECT u FROM Universite u JOIN u.medecins m WHERE m.id = :doctorId")
    List<Universite> findByMedecinId(@Param("doctorId") String doctorId);

    // Find first university by doctor ID (if you want just one)
    @Query("SELECT u FROM Universite u JOIN u.medecins m WHERE m.id = :doctorId")
    Optional<Universite> findFirstByMedecinId(@Param("doctorId") String doctorId);

    // Alternative using derived query
    List<Universite> findByMedecins_Id(String doctorId);
}