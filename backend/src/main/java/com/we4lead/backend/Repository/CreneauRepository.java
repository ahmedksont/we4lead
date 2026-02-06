package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Creneau;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreneauRepository extends JpaRepository<Creneau, String> {
    List<Creneau> findByMedecinId(String medecinId);
}
