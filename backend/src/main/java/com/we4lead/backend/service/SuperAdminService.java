package com.we4lead.backend.service;

import com.we4lead.backend.SupabaseAuthService;
import com.we4lead.backend.dto.UniversiteRequest;
import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.Repository.UniversiteRepository;
import com.we4lead.backend.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class SuperAdminService {

    private final UserRepository userRepository;
    private final UniversiteRepository universiteRepository;
    private final String uploadDir = "uploads/universites";
    private final SupabaseAuthService supabaseAuthService;

    public SuperAdminService(UserRepository userRepository, UniversiteRepository universiteRepository, SupabaseAuthService supabaseAuthService) {
        this.userRepository = userRepository;
        this.universiteRepository = universiteRepository;
        this.supabaseAuthService = supabaseAuthService;

        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create upload folder", e);
            }
        }
    }



    public Universite createUniversite(UniversiteRequest request) throws IOException {
        Universite uni = new Universite();
        mapRequestToUniversite(request, uni);

        uni.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        return universiteRepository.save(uni);
    }



    public List<Universite> getAllUniversites() {
        return universiteRepository.findAll();
    }

    public Universite getUniversiteById(Long id) {
        return universiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Universite not found"));
    }


    public Universite updateUniversite(Long id, UniversiteRequest request) throws IOException {
        Universite uni = getUniversiteById(id);

        mapRequestToUniversite(request, uni);

        return universiteRepository.save(uni);
    }



    public void deleteUniversite(Long id) {
        if (!universiteRepository.existsById(id)) {
            throw new RuntimeException("Universite not found");
        }

        universiteRepository.deleteById(id);
    }
    private void mapRequestToUniversite(UniversiteRequest request, Universite uni) throws IOException {
        uni.setNom(request.getNom());
        uni.setVille(request.getVille());
        uni.setAdresse(request.getAdresse());
        uni.setTelephone(request.getTelephone());
        uni.setNbEtudiants(request.getNbEtudiants());
        uni.setHoraire(request.getHoraire());

        MultipartFile logo = request.getLogo();
        if (logo != null && !logo.isEmpty()) {
            if (uni.getLogoPath() != null) {
                Path oldFilePath = Paths.get(uploadDir, uni.getLogoPath());
                Files.deleteIfExists(oldFilePath);
            }
            String originalFilename = StringUtils.cleanPath(logo.getOriginalFilename());
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = uni.getNom().replaceAll("\\s+", "_") + "_" +
                    System.currentTimeMillis() + extension;

            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, logo.getBytes());
            uni.setLogoPath(filename);
        }
    }
    @Transactional
    public User createAdmin(UserCreateRequest request) {
        Universite universite = universiteRepository.findById(request.getUniversiteId())
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + request.getUniversiteId()));

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());
        user.setRole(Role.ADMIN);
        user.setUniversite(universite);
        User savedUser = userRepository.save(user);

        supabaseAuthService.inviteUser(savedUser.getEmail());

        return savedUser;
    }

    public User getAdminById(String id) {
        return userRepository.findById(id)
                .filter(user -> user.getRole() == Role.ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé avec l'ID : " + id));
    }

    public User updateAdmin(String id, UserCreateRequest request) {
        User user = getAdminById(id);
        if (request.getNom() != null) {
            user.setNom(request.getNom());
        }
        if (request.getPrenom() != null) {
            user.setPrenom(request.getPrenom());
        }
        if (request.getTelephone() != null) {
            user.setTelephone(request.getTelephone());
        }

        return userRepository.save(user);
    }

    public void deleteAdmin(String id) {
        User admin = getAdminById(id);
        userRepository.delete(admin);
    }
    public List<User> getAllAdmins() {
        return userRepository.findByRoleWithUniversity(Role.ADMIN);
    }
}