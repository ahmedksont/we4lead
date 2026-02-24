package com.we4lead.backend.service;

import com.we4lead.backend.SupabaseAuthService;
import com.we4lead.backend.dto.UniversiteRequest;
import com.we4lead.backend.dto.UserCreateRequest;
import com.we4lead.backend.dto.UserResponse;
import com.we4lead.backend.dto.UniversiteResponse;
import com.we4lead.backend.entity.Role;
import com.we4lead.backend.entity.Universite;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.Repository.UniversiteRepository;
import com.we4lead.backend.Repository.UserRepository;
import com.we4lead.backend.Repository.DemandeRepository; // AJOUT
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SuperAdminService {

    private final UserRepository userRepository;
    private final UniversiteRepository universiteRepository;
    private final DemandeRepository demandeRepository; // AJOUT
    private final UrlService urlService; // AJOUT
    private final String uploadDir = "uploads/universites";
    private final SupabaseAuthService supabaseAuthService;

    public SuperAdminService(
            UserRepository userRepository,
            UniversiteRepository universiteRepository,
            DemandeRepository demandeRepository, // AJOUT
            UrlService urlService, // AJOUT
            SupabaseAuthService supabaseAuthService) {
        this.userRepository = userRepository;
        this.universiteRepository = universiteRepository;
        this.demandeRepository = demandeRepository;
        this.urlService = urlService;
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

    // ==================== UNIVERSITES CRUD ====================

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

    // ==================== ADMINS CRUD ====================

    @Transactional
    public User createAdmin(UserCreateRequest request) {
        if (request.getUniversiteIds() == null || request.getUniversiteIds().isEmpty()) {
            throw new IllegalArgumentException("Au moins une université est obligatoire pour créer un admin");
        }

        Universite universite = universiteRepository.findById(request.getUniversiteIds().get(0))
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + request.getUniversiteIds().get(0)));

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

        if (request.getUniversiteIds() != null && !request.getUniversiteIds().isEmpty()) {
            Universite universite = universiteRepository.findById(request.getUniversiteIds().get(0))
                    .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + request.getUniversiteIds().get(0)));
            user.setUniversite(universite);
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

    // ==================== USERS CRUD AVEC NOMBRE DE DEMANDES ====================

    /**
     * Récupère tous les utilisateurs (tous rôles confondus)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Récupère tous les utilisateurs avec leurs universités
     */
    public List<User> getAllUsersWithUniversities() {
        return userRepository.findAllWithUniversities();
    }

    public User getUserById(String id) {
        return userRepository.findByIdWithUniversities(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + id));
    }

    /**
     * Récupère tous les utilisateurs avec leur nombre de demandes
     */
    public List<UserResponse> getAllUsersWithDemandesCount() {
        List<User> users = userRepository.findAllWithUniversities();

        // Récupérer tous les comptages en une seule requête optimisée
        List<Object[]> counts = demandeRepository.countAllDemandesByUser();
        Map<String, Long> demandesCountMap = counts.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));

        return users.stream()
                .map(user -> mapToUserResponseWithDemandes(
                        user,
                        demandesCountMap.getOrDefault(user.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }

    /**
     * Récupère un utilisateur avec son nombre de demandes
     */
    public UserResponse getUserWithDemandesCount(String id) {
        User user = userRepository.findByIdWithUniversities(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + id));

        long nombreDemandes = demandeRepository.countByEtudiantId(id);

        return mapToUserResponseWithDemandes(user, nombreDemandes);
    }

    /**
     * Récupère les utilisateurs par rôle avec leur nombre de demandes
     */
    public List<UserResponse> getUsersByRoleWithDemandesCount(Role role) {
        List<User> users = userRepository.findByRoleWithUniversities(role);

        List<String> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // Créer une map finale pour les comptages
        final Map<String, Long> demandesCountMap = new HashMap<>();

        if (!userIds.isEmpty()) {
            List<Object[]> counts = demandeRepository.countDemandesByUserIds(userIds);

            // Remplir la map sans utiliser de lambda qui modifie une variable externe
            for (Object[] count : counts) {
                String userId = (String) count[0];
                Long nombre = (Long) count[1];
                demandesCountMap.put(userId, nombre);
            }
        }

        return users.stream()
                .map(user -> mapToUserResponseWithDemandes(
                        user,
                        demandesCountMap.getOrDefault(user.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }
    /**
     * Convertit un User en UserResponse avec le nombre de demandes
     */
    private UserResponse mapToUserResponseWithDemandes(User user, long nombreDemandes) {
        UniversiteResponse universiteResponse = null;
        if (user.getUniversite() != null) {
            universiteResponse = new UniversiteResponse(
                    user.getUniversite().getId(),
                    user.getUniversite().getNom(),
                    user.getUniversite().getVille(),
                    user.getUniversite().getAdresse(),
                    user.getUniversite().getTelephone(),
                    user.getUniversite().getNbEtudiants(),
                    user.getUniversite().getHoraire(),
                    urlService.getUniversiteLogoUrl(user.getUniversite().getLogoPath()),
                    user.getUniversite().getCode()
            );
        }

        List<UniversiteResponse> universitesResponses = user.getUniversites().stream()
                .map(u -> new UniversiteResponse(
                        u.getId(),
                        u.getNom(),
                        u.getVille(),
                        u.getAdresse(),
                        u.getTelephone(),
                        u.getNbEtudiants(),
                        u.getHoraire(),
                        urlService.getUniversiteLogoUrl(u.getLogoPath()),
                        u.getCode()
                ))
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getTelephone(),
                user.getRole(),
                urlService.getPhotoUrl(user.getPhotoPath()),
                user.getSpecialite(),
                user.getGenre(),
                user.getSituation(),
                user.getNiveauEtude(),
                universiteResponse,
                universitesResponses,
                nombreDemandes
        );
    }
}