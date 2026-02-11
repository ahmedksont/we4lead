package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.universites WHERE u.role = :role")
    List<User> findByRoleWithUniversities(@Param("role") Role role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.universites WHERE u.id = :id")
    Optional<User> findByIdWithUniversities(@Param("id") String id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.universite WHERE u.role = :role")
    List<User> findByRoleWithUniversity(@Param("role") Role role);
}
