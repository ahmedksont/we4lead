package com.we4lead.backend.service;

import com.we4lead.backend.dto.UniversiteRequest;
import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.Repository.UniversiteRepository;
import com.we4lead.backend.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class SuperAdminService {

    private final UserRepository userRepository;
    private final UniversiteRepository universiteRepository;
    private final String uploadDir = "uploads/universites";

    public SuperAdminService(UserRepository userRepository, UniversiteRepository universiteRepository) {
        this.userRepository = userRepository;
        this.universiteRepository = universiteRepository;

        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            try { Files.createDirectories(path); }
            catch (IOException e) { throw new RuntimeException("Cannot create upload folder", e); }
        }
    }

    public Universite createUniversite(UniversiteRequest request) throws IOException {
        Universite uni = new Universite();
        uni.setNom(request.getNom());
        uni.setVille(request.getVille());
        uni.setAdresse(request.getAdresse());
        uni.setTelephone(request.getTelephone());
        uni.setNbEtudiants(request.getNbEtudiants());
        uni.setHoraire(request.getHoraire());
        uni.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Handle logo upload
        MultipartFile logo = request.getLogo();
        if (logo != null && !logo.isEmpty()) {
            String filename = StringUtils.cleanPath(uni.getNom().replaceAll("\\s+", "_") + "_" + logo.getOriginalFilename());
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, logo.getBytes());
            uni.setLogoPath("/universites/logos/" + filename); // URL to serve frontend
        }

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
