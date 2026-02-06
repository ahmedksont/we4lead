package com.we4lead.backend.service;

import com.we4lead.backend.dto.UniversiteRequest;
import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.Repository.UniversiteRepository;
import com.we4lead.backend.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SuperAdminService {

    private final UserRepository userRepository;
    private final UniversiteRepository universiteRepository;

    public SuperAdminService(UserRepository userRepository, UniversiteRepository universiteRepository) {
        this.userRepository = userRepository;
        this.universiteRepository = universiteRepository;
    }

    public Universite createUniversite(UniversiteRequest request) {
        Universite uni = new Universite();
        uni.setNom(request.getNom());
        uni.setVille(request.getVille());
        uni.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return universiteRepository.save(uni);
    }
    public User createAdmin(UserCreateRequest request) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }
}
