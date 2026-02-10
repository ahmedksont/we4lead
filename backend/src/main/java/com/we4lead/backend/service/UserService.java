package com.we4lead.backend.service;

import com.we4lead.backend.Repository.CreneauRepository;
import com.we4lead.backend.Repository.RdvRepository;
import com.we4lead.backend.Repository.UserRepository;
import com.we4lead.backend.dto.CreneauResponse;
import com.we4lead.backend.dto.MedecinResponse;
import com.we4lead.backend.dto.RdvResponse;
import com.we4lead.backend.dto.UserUpdateRequest;
import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CreneauRepository creneauRepository;
    private final RdvRepository rdvRepository;  // ✅ Inject RdvRepository

    private final String uploadDir = "uploads";

    public UserService(UserRepository userRepository,
                       CreneauRepository creneauRepository,
                       RdvRepository rdvRepository) {  // ✅ constructor

        this.userRepository = userRepository;
        this.creneauRepository = creneauRepository;
        this.rdvRepository = rdvRepository;

        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Could not create upload folder", e);
            }
        }
    }

    // ================= AUTH SYNC =================

    @Transactional
    public User syncUser(Jwt jwt){
        String id = jwt.getSubject();
        String email = jwt.getClaim("email");

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        if (user == null) {
            user = new User();
            user.setId(id);
            user.setEmail(email);
            user.setRole(Role.ETUDIANT);
            user = userRepository.save(user);
        }

        return user;
    }

    // ================= PROFILE =================

    public User updateUser(Jwt jwt, UserUpdateRequest request) {

        User user = userRepository.findById(jwt.getSubject())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getNom() != null) user.setNom(request.getNom());
        if (request.getPrenom() != null) user.setPrenom(request.getPrenom());
        if (request.getTelephone() != null) user.setTelephone(request.getTelephone());

        return userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // ================= PHOTO =================

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

    // ================= MEDECINS =================

    public List<User> getAllMedecins() {
        return userRepository.findByRole(Role.MEDECIN);
    }

    public List<MedecinResponse> getMedecinsWithAppointments() {
        List<User> medecins = userRepository.findByRole(Role.MEDECIN);

        return medecins.stream().map(m -> {

            // Creneaux (working hours)
            List<CreneauResponse> creneaux = creneauRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(c -> new CreneauResponse(c.getId(), c.getJour(), c.getDebut(), c.getFin()))
                    .toList();

            // Appointments (rdvs)
            List<RdvResponse> rdvs = rdvRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(r -> new RdvResponse(r.getId(), r.getDate(), r.getHeure(), r.getEtudiant().getNom()))
                    .toList();

            return new MedecinResponse(
                    m.getId(),
                    m.getNom(),
                    m.getPrenom(),
                    m.getEmail(),
                    m.getPhotoPath() != null ? "/users/me/photo" : null,
                    creneaux,
                    rdvs
            );

        }).toList();
    }
}
