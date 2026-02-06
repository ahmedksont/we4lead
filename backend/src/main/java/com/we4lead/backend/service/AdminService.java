package com.we4lead.backend.service;

import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public User createMedecin(UserCreateRequest request) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());
        user.setRole(Role.MEDECIN);
        return userRepository.save(user);
    }
}
