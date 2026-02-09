package com.we4lead.backend.Repository;

import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    List<User> findByRole(Role role);
}
