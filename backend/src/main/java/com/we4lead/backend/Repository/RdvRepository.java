package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Rdv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RdvRepository extends JpaRepository<Rdv, String> {

    boolean existsByMedecin_IdAndDateAndHeure(String medecinId, String date, String heure);
    List<Rdv> findByMedecin_Id(String medecinId);

    List<Rdv> findByEtudiant_Id(String etudiantId);

}
