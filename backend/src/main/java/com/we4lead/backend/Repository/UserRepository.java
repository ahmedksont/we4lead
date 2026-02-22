package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.universites WHERE u.role = :role")
    List<User> findByRoleWithUniversities(@Param("role") Role role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.universites WHERE u.id = :id")
    Optional<User> findByIdWithUniversities(@Param("id") String id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.universite WHERE u.role = :role")
    List<User> findByRoleWithUniversity(@Param("role") Role role);

    @Query("SELECT u FROM User u JOIN u.universites univ WHERE univ.id = :universiteId AND u.role = :role")
    List<User> findByUniversiteIdAndRole(@Param("universiteId") Long universiteId, @Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.universite.id = :universiteId")
    List<User> findEtudiantsByUniversiteId(@Param("universiteId") Long universiteId, @Param("role") Role role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.universite WHERE u.role = :role")
    List<User> findEtudiantsWithUniversity(@Param("role") Role role);

    // ✅ CORRECTION 1: Une seule méthode countByRole (pas de surcharge)
    long countByRole(Role role);

    // ✅ CORRECTION 2: Méthode pour compter par rôle et université (pour les admins d'université)
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.universite.id = :universiteId")
    long countByRoleAndUniversiteId(@Param("role") Role role, @Param("universiteId") Long universiteId);

    // ✅ CORRECTION 3: Méthode pour compter les médecins par université (relation ManyToMany)
    @Query("SELECT COUNT(u) FROM User u JOIN u.universites univ WHERE univ.id = :universiteId AND u.role = :role")
    long countMedecinsByUniversiteId(@Param("universiteId") Long universiteId, @Param("role") Role role);

    // ✅ CORRECTION 4: Vérifier si un email existe déjà
    boolean existsByEmail(String email);
    
}