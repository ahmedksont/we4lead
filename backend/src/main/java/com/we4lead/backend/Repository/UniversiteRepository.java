package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Universite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversiteRepository extends JpaRepository<Universite, Long> {
    Universite findByNom(String nom);
}
