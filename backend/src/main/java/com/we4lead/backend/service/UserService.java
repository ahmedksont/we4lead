package com.we4lead.backend.service;

import com.we4lead.backend.dto.UserUpdateRequest;
import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final String uploadDir = "uploads";

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Could not create upload folder", e);
            }
        }
    }

    @Transactional
    public User syncUser(Jwt jwt){
        String id = jwt.getSubject();
        String email = jwt.getClaim("email");

        return userRepository.findById(id)
                .orElseGet(() -> userRepository.save(
                        new User(
                                id,
                                email,
                                null,
                                null,
                                null,
                                Role.ETUDIANT
                        )
                ));
    }

    public User updateUser(Jwt jwt, UserUpdateRequest request) {
        String id = jwt.getSubject();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getNom() != null) user.setNom(request.getNom());
        if (request.getPrenom() != null) user.setPrenom(request.getPrenom());
        if (request.getTelephone() != null) user.setTelephone(request.getTelephone());

        return userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
    public User uploadPhoto(Jwt jwt, MultipartFile file) throws IOException {
        User user = syncUser(jwt);
        if (user.getPhotoPath() != null) {
            Files.deleteIfExists(Paths.get(user.getPhotoPath()));
        }

        String filename = user.getId() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        Files.write(filePath, file.getBytes());

        user.setPhotoPath(filePath.toString());
        return userRepository.save(user);
    }
}
