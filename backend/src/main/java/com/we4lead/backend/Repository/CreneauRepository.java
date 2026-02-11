package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Creneau;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CreneauRepository extends JpaRepository<Creneau, String> {
    List<Creneau> findByMedecin_Id(String medecinId);
    @Modifying
    @Transactional
    @Query("DELETE FROM Creneau c WHERE c.medecin.id = :medecinId")
    void deleteByMedecin_Id(@Param("medecinId") String medecinId);
}
