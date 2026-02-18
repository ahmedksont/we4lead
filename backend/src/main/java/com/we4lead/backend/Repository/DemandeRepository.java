package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Demande;
import com.we4lead.backend.entity.TypeSituation;
import org.springframework.data.jpa.repository.JpaRepository;
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
}