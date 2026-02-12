package com.we4lead.backend.service;

import com.we4lead.backend.Repository.CreneauRepository;
import com.we4lead.backend.Repository.RdvRepository;
import com.we4lead.backend.Repository.UniversiteRepository;
import com.we4lead.backend.Repository.UserRepository;
import com.we4lead.backend.SupabaseAuthService;
import com.we4lead.backend.dto.*;
import com.we4lead.backend.entity.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final UniversiteRepository universiteRepository;
    private final CreneauRepository creneauRepository;
    private final RdvRepository rdvRepository;
    private final SupabaseAuthService supabaseAuthService;

    public AdminService(
            UserRepository userRepository,
            UniversiteRepository universiteRepository,
            CreneauRepository creneauRepository,
            RdvRepository rdvRepository,
            SupabaseAuthService supabaseAuthService) {
        this.userRepository = userRepository;
        this.universiteRepository = universiteRepository;
        this.creneauRepository = creneauRepository;
        this.rdvRepository = rdvRepository;
        this.supabaseAuthService = supabaseAuthService;
    }

    @Transactional
    public User createMedecin(UserCreateRequest request) {
        // Validate university ID
        if (request.getUniversiteId() == null) {
            throw new IllegalArgumentException("L'université est obligatoire pour créer un médecin");
        }

        // Find the university
        Universite universite = universiteRepository.findById(request.getUniversiteId())
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + request.getUniversiteId()));

        // Create new medicin user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());
        user.setRole(Role.MEDECIN);

        // Assign university to medicin (many-to-many)
        Set<Universite> universites = new HashSet<>();
        universites.add(universite);
        user.setUniversites(universites);

        // Save the user
        User savedUser = userRepository.save(user);

        // Invite the user via Supabase
        supabaseAuthService.inviteUser(savedUser.getEmail());

        return savedUser;
    }

    public List<MedecinResponse> getAllMedecins() {
        List<User> medecins = userRepository.findByRole(Role.MEDECIN);

        return medecins.stream().map(m -> {
            List<CreneauResponse> creneaux = creneauRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(c -> new CreneauResponse(c.getId(), c.getJour(), c.getDebut(), c.getFin()))
                    .toList();

            // FIX: Update RdvResponse creation to use FULL constructor
            List<RdvResponse> rdvs = rdvRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(r -> {
                        // Create FULL MedecinResponse for the RDV
                        List<UniversiteResponse> medecinUniversites = r.getMedecin().getUniversites().stream()
                                .map(u -> new UniversiteResponse(
                                        u.getId(),
                                        u.getNom(),
                                        u.getVille(),
                                        u.getAdresse(),
                                        u.getTelephone(),
                                        u.getNbEtudiants(),
                                        u.getHoraire(),
                                        u.getLogoPath(),
                                        u.getCode()
                                ))
                                .toList();

                        MedecinResponse rdvMedecinResponse = new MedecinResponse(
                                r.getMedecin().getId(),
                                r.getMedecin().getNom(),
                                r.getMedecin().getPrenom(),
                                r.getMedecin().getEmail(),
                                r.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                                r.getMedecin().getTelephone(),
                                medecinUniversites,
                                List.of(),
                                List.of()
                        );

                        // Create FULL EtudiantResponse for the RDV
                        UniversiteResponse etudiantUniversite = null;
                        if (r.getEtudiant() != null && r.getEtudiant().getUniversite() != null) {
                            etudiantUniversite = new UniversiteResponse(
                                    r.getEtudiant().getUniversite().getId(),
                                    r.getEtudiant().getUniversite().getNom(),
                                    r.getEtudiant().getUniversite().getVille(),
                                    r.getEtudiant().getUniversite().getAdresse(),
                                    r.getEtudiant().getUniversite().getTelephone(),
                                    r.getEtudiant().getUniversite().getNbEtudiants(),
                                    r.getEtudiant().getUniversite().getHoraire(),
                                    r.getEtudiant().getUniversite().getLogoPath(),
                                    r.getEtudiant().getUniversite().getCode()
                            );
                        }

                        EtudiantResponse etudiantResponse = r.getEtudiant() != null ?
                                new EtudiantResponse(
                                        r.getEtudiant().getId(),
                                        r.getEtudiant().getNom(),
                                        r.getEtudiant().getPrenom(),
                                        r.getEtudiant().getEmail(),
                                        r.getEtudiant().getTelephone(),
                                        r.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                                        etudiantUniversite
                                ) : null;

                        return new RdvResponse(
                                r.getId(),
                                r.getDate(),
                                r.getHeure(),
                                r.getStatus() != null ? r.getStatus().name() : "CONFIRMED",
                                rdvMedecinResponse,
                                etudiantResponse
                        );
                    })
                    .toList();

            // Convert universities to UniversiteResponse records
            List<UniversiteResponse> universiteResponses = m.getUniversites().stream()
                    .map(u -> new UniversiteResponse(
                            u.getId(),
                            u.getNom(),
                            u.getVille(),
                            u.getAdresse(),
                            u.getTelephone(),
                            u.getNbEtudiants(),
                            u.getHoraire(),
                            u.getLogoPath(),
                            u.getCode()
                    ))
                    .toList();

            return new MedecinResponse(
                    m.getId(),
                    m.getNom(),
                    m.getPrenom(),
                    m.getEmail(),
                    m.getPhotoPath() != null ? "/users/me/photo" : null,
                    m.getTelephone(),
                    universiteResponses,
                    creneaux,
                    rdvs
            );
        }).toList();
    }
    public User getMedecinById(String id) {
        return userRepository.findById(id)
                .filter(user -> user.getRole() == Role.MEDECIN)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'ID : " + id));
    }

    @Transactional
    public User updateMedecin(String id, UserCreateRequest request) {
        User user = getMedecinById(id);

        if (request.getNom() != null) {
            user.setNom(request.getNom());
        }
        if (request.getPrenom() != null) {
            user.setPrenom(request.getPrenom());
        }
        if (request.getTelephone() != null) {
            user.setTelephone(request.getTelephone());
        }

        // Update university if provided
        if (request.getUniversiteId() != null) {
            Universite universite = universiteRepository.findById(request.getUniversiteId())
                    .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + request.getUniversiteId()));

            Set<Universite> universites = new HashSet<>();
            universites.add(universite);
            user.setUniversites(universites);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteMedecin(String id, boolean forceCascade) {
        User medecin = getMedecinById(id);

        // Check if medicin has appointments
        long appointmentsCount = rdvRepository.countByMedecin_Id(id);

        if (appointmentsCount > 0 && !forceCascade) {
            throw new RuntimeException("Ce médecin a " + appointmentsCount + " rendez-vous. Utilisez forceCascade=true pour supprimer quand même.");
        }

        // Delete related entities
        if (forceCascade) {
            rdvRepository.deleteByMedecin_Id(id);
            creneauRepository.deleteByMedecin_Id(id);
        }

        userRepository.delete(medecin);
    }
    public List<MedecinResponse> getMedecinsByUniversiteId(Long universiteId) {
        // Verify university exists
        Universite universite = universiteRepository.findById(universiteId)
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + universiteId));

        // Get all medecins for this university
        List<User> medecins = userRepository.findByUniversiteIdAndRole(universiteId, Role.MEDECIN);

        return medecins.stream().map(m -> {
            List<CreneauResponse> creneaux = creneauRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(c -> new CreneauResponse(c.getId(), c.getJour(), c.getDebut(), c.getFin()))
                    .toList();

            // FIX: Update RdvResponse creation to use FULL constructor
            List<RdvResponse> rdvs = rdvRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(r -> {
                        // Create FULL MedecinResponse for the RDV
                        List<UniversiteResponse> medecinUniversites = r.getMedecin().getUniversites().stream()
                                .map(u -> new UniversiteResponse(
                                        u.getId(),
                                        u.getNom(),
                                        u.getVille(),
                                        u.getAdresse(),
                                        u.getTelephone(),
                                        u.getNbEtudiants(),
                                        u.getHoraire(),
                                        u.getLogoPath(),
                                        u.getCode()
                                ))
                                .toList();

                        MedecinResponse rdvMedecinResponse = new MedecinResponse(
                                r.getMedecin().getId(),
                                r.getMedecin().getNom(),
                                r.getMedecin().getPrenom(),
                                r.getMedecin().getEmail(),
                                r.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                                r.getMedecin().getTelephone(),
                                medecinUniversites,
                                List.of(),
                                List.of()
                        );

                        // Create FULL EtudiantResponse for the RDV
                        UniversiteResponse etudiantUniversite = null;
                        if (r.getEtudiant() != null && r.getEtudiant().getUniversite() != null) {
                            etudiantUniversite = new UniversiteResponse(
                                    r.getEtudiant().getUniversite().getId(),
                                    r.getEtudiant().getUniversite().getNom(),
                                    r.getEtudiant().getUniversite().getVille(),
                                    r.getEtudiant().getUniversite().getAdresse(),
                                    r.getEtudiant().getUniversite().getTelephone(),
                                    r.getEtudiant().getUniversite().getNbEtudiants(),
                                    r.getEtudiant().getUniversite().getHoraire(),
                                    r.getEtudiant().getUniversite().getLogoPath(),
                                    r.getEtudiant().getUniversite().getCode()
                            );
                        }

                        EtudiantResponse etudiantResponse = r.getEtudiant() != null ?
                                new EtudiantResponse(
                                        r.getEtudiant().getId(),
                                        r.getEtudiant().getNom(),
                                        r.getEtudiant().getPrenom(),
                                        r.getEtudiant().getEmail(),
                                        r.getEtudiant().getTelephone(),
                                        r.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                                        etudiantUniversite
                                ) : null;

                        return new RdvResponse(
                                r.getId(),
                                r.getDate(),
                                r.getHeure(),
                                r.getStatus() != null ? r.getStatus().name() : "CONFIRMED",
                                rdvMedecinResponse,
                                etudiantResponse
                        );
                    })
                    .toList();

            // Convert universities to UniversiteResponse
            List<UniversiteResponse> universiteResponses = m.getUniversites().stream()
                    .map(u -> new UniversiteResponse(
                            u.getId(),
                            u.getNom(),
                            u.getVille(),
                            u.getAdresse(),
                            u.getTelephone(),
                            u.getNbEtudiants(),
                            u.getHoraire(),
                            u.getLogoPath(),
                            u.getCode()
                    ))
                    .toList();

            return new MedecinResponse(
                    m.getId(),
                    m.getNom(),
                    m.getPrenom(),
                    m.getEmail(),
                    m.getPhotoPath() != null ? "/users/me/photo" : null,
                    m.getTelephone(),
                    universiteResponses,
                    creneaux,
                    rdvs
            );
        }).toList();
    }
    @Transactional
    public User createEtudiant(UserCreateRequest request) {
        // Validate university ID
        if (request.getUniversiteId() == null) {
            throw new IllegalArgumentException("L'université est obligatoire pour créer un étudiant");
        }

        // Find the university
        Universite universite = universiteRepository.findById(request.getUniversiteId())
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + request.getUniversiteId()));

        // Create new etudiant user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());
        user.setRole(Role.ETUDIANT);

        // Assign university to etudiant (many-to-one)
        user.setUniversite(universite);

        // Save the user
        User savedUser = userRepository.save(user);

        // Invite the user via Supabase
        supabaseAuthService.inviteUser(savedUser.getEmail());

        return savedUser;
    }

    public List<EtudiantResponse> getAllEtudiants() {
        List<User> etudiants = userRepository.findByRole(Role.ETUDIANT);

        return etudiants.stream().map(e -> {
            UniversiteResponse universiteResponse = null;
            if (e.getUniversite() != null) {
                universiteResponse = new UniversiteResponse(
                        e.getUniversite().getId(),
                        e.getUniversite().getNom(),
                        e.getUniversite().getVille(),
                        e.getUniversite().getAdresse(),
                        e.getUniversite().getTelephone(),
                        e.getUniversite().getNbEtudiants(),
                        e.getUniversite().getHoraire(),
                        e.getUniversite().getLogoPath(),
                        e.getUniversite().getCode()
                );
            }

            return new EtudiantResponse(
                    e.getId(),
                    e.getNom(),
                    e.getPrenom(),
                    e.getEmail(),
                    e.getTelephone(),
                    e.getPhotoPath() != null ? "/users/me/photo" : null,
                    universiteResponse
            );
        }).toList();
    }

    public List<EtudiantResponse> getEtudiantsByUniversiteId(Long universiteId) {
        // Verify university exists
        Universite universite = universiteRepository.findById(universiteId)
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + universiteId));

        // Get all etudiants for this university
        List<User> etudiants = userRepository.findEtudiantsByUniversiteId(universiteId, Role.ETUDIANT);

        return etudiants.stream().map(e -> {
            UniversiteResponse universiteResponse = new UniversiteResponse(
                    e.getUniversite().getId(),
                    e.getUniversite().getNom(),
                    e.getUniversite().getVille(),
                    e.getUniversite().getAdresse(),
                    e.getUniversite().getTelephone(),
                    e.getUniversite().getNbEtudiants(),
                    e.getUniversite().getHoraire(),
                    e.getUniversite().getLogoPath(),
                    e.getUniversite().getCode()
            );

            return new EtudiantResponse(
                    e.getId(),
                    e.getNom(),
                    e.getPrenom(),
                    e.getEmail(),
                    e.getTelephone(),
                    e.getPhotoPath() != null ? "/users/me/photo" : null,
                    universiteResponse
            );
        }).toList();
    }

    public User getEtudiantById(String id) {
        return userRepository.findById(id)
                .filter(user -> user.getRole() == Role.ETUDIANT)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'ID : " + id));
    }

    @Transactional
    public User updateEtudiant(String id, UserCreateRequest request) {
        User user = getEtudiantById(id);

        if (request.getNom() != null) {
            user.setNom(request.getNom());
        }
        if (request.getPrenom() != null) {
            user.setPrenom(request.getPrenom());
        }
        if (request.getTelephone() != null) {
            user.setTelephone(request.getTelephone());
        }

        // Update university if provided
        if (request.getUniversiteId() != null) {
            Universite universite = universiteRepository.findById(request.getUniversiteId())
                    .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + request.getUniversiteId()));
            user.setUniversite(universite);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteEtudiant(String id) {
        User etudiant = getEtudiantById(id);

        // Check if student has appointments
        long appointmentsCount = rdvRepository.countByEtudiant_Id(id);

        if (appointmentsCount > 0) {
            // Delete related appointments first
            rdvRepository.deleteByEtudiant_Id(id);
        }

        userRepository.delete(etudiant);
    }
    // ================= RDV (APPOINTMENTS) CRUD =================

    @Transactional
    public Rdv createRdv(RdvRequest request) {
        // Validate required fields
        if (request.getMedecinId() == null) {
            throw new IllegalArgumentException("Médecin est obligatoire");
        }
        if (request.getEtudiantId() == null) {
            throw new IllegalArgumentException("Étudiant est obligatoire");
        }
        if (request.getDate() == null || request.getHeure() == null) {
            throw new IllegalArgumentException("Date et heure sont obligatoires");
        }

        // Find medecin
        User medecin = userRepository.findById(request.getMedecinId())
                .filter(user -> user.getRole() == Role.MEDECIN)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé : " + request.getMedecinId()));

        // Find etudiant
        User etudiant = userRepository.findById(request.getEtudiantId())
                .filter(user -> user.getRole() == Role.ETUDIANT)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé : " + request.getEtudiantId()));

        // Check if the etudiant belongs to the same university as the medecin
        boolean sameUniversity = medecin.getUniversites().stream()
                .anyMatch(u -> u.getId().equals(etudiant.getUniversite().getId()));

        if (!sameUniversity) {
            throw new IllegalArgumentException("L'étudiant et le médecin doivent appartenir à la même université");
        }

        // Check if the slot is already taken
        boolean slotTaken = rdvRepository.existsByMedecin_IdAndDateAndHeure(
                request.getMedecinId(),
                request.getDate(),
                request.getHeure()
        );

        if (slotTaken) {
            throw new IllegalArgumentException("Ce créneau est déjà réservé pour ce médecin");
        }

        // Create new RDV with student assigned directly
        Rdv rdv = new Rdv();
        rdv.setId(UUID.randomUUID().toString());
        rdv.setDate(request.getDate());
        rdv.setHeure(request.getHeure());
        rdv.setMedecin(medecin);
        rdv.setEtudiant(etudiant);
        rdv.setStatus(RdvStatus.CONFIRMED);

        return rdvRepository.save(rdv);
    }
    @Transactional
    public Rdv assignEtudiantToRdv(String rdvId, String etudiantId) {
        Rdv rdv = rdvRepository.findById(rdvId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé avec l'ID : " + rdvId));

        User etudiant = userRepository.findById(etudiantId)
                .filter(user -> user.getRole() == Role.ETUDIANT)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé : " + etudiantId));

        rdv.setEtudiant(etudiant);
        return rdvRepository.save(rdv);
    }

    public List<RdvResponse> getAllRdvs() {
        List<Rdv> rdvs = rdvRepository.findAll();

        return rdvs.stream().map(r -> {
            // Convert doctor universities
            List<UniversiteResponse> medecinUniversites = r.getMedecin().getUniversites().stream()
                    .map(u -> new UniversiteResponse(
                            u.getId(),
                            u.getNom(),
                            u.getVille(),
                            u.getAdresse(),
                            u.getTelephone(),
                            u.getNbEtudiants(),
                            u.getHoraire(),
                            u.getLogoPath(),
                            u.getCode()
                    ))
                    .toList();

            // Create FULL MedecinResponse
            MedecinResponse medecinResponse = new MedecinResponse(
                    r.getMedecin().getId(),
                    r.getMedecin().getNom(),
                    r.getMedecin().getPrenom(),
                    r.getMedecin().getEmail(),
                    r.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                    r.getMedecin().getTelephone(),
                    medecinUniversites,
                    List.of(), // creneaux - empty list
                    List.of()  // rdvs - empty list to avoid circular reference
            );

            // Convert student university
            UniversiteResponse etudiantUniversite = null;
            if (r.getEtudiant() != null && r.getEtudiant().getUniversite() != null) {
                etudiantUniversite = new UniversiteResponse(
                        r.getEtudiant().getUniversite().getId(),
                        r.getEtudiant().getUniversite().getNom(),
                        r.getEtudiant().getUniversite().getVille(),
                        r.getEtudiant().getUniversite().getAdresse(),
                        r.getEtudiant().getUniversite().getTelephone(),
                        r.getEtudiant().getUniversite().getNbEtudiants(),
                        r.getEtudiant().getUniversite().getHoraire(),
                        r.getEtudiant().getUniversite().getLogoPath(),
                        r.getEtudiant().getUniversite().getCode()
                );
            }

            // Create FULL EtudiantResponse
            EtudiantResponse etudiantResponse = new EtudiantResponse(
                    r.getEtudiant().getId(),
                    r.getEtudiant().getNom(),
                    r.getEtudiant().getPrenom(),
                    r.getEtudiant().getEmail(),
                    r.getEtudiant().getTelephone(),
                    r.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                    etudiantUniversite
            );

            return new RdvResponse(
                    r.getId(),
                    r.getDate(),
                    r.getHeure(),
                    r.getStatus() != null ? r.getStatus().name() : "CONFIRMED",
                    medecinResponse,
                    etudiantResponse
            );
        }).toList();
    }

    public RdvResponse getRdvById(String id) {
        Rdv rdv = rdvRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé avec l'ID : " + id));

        // Convert doctor universities
        List<UniversiteResponse> medecinUniversites = rdv.getMedecin().getUniversites().stream()
                .map(u -> new UniversiteResponse(
                        u.getId(),
                        u.getNom(),
                        u.getVille(),
                        u.getAdresse(),
                        u.getTelephone(),
                        u.getNbEtudiants(),
                        u.getHoraire(),
                        u.getLogoPath(),
                        u.getCode()
                ))
                .toList();

        // Create FULL MedecinResponse
        MedecinResponse medecinResponse = new MedecinResponse(
                rdv.getMedecin().getId(),
                rdv.getMedecin().getNom(),
                rdv.getMedecin().getPrenom(),
                rdv.getMedecin().getEmail(),
                rdv.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                rdv.getMedecin().getTelephone(),
                medecinUniversites,
                List.of(), // creneaux - empty list
                List.of()  // rdvs - empty list to avoid circular reference
        );

        // Convert student university
        UniversiteResponse etudiantUniversite = null;
        if (rdv.getEtudiant() != null && rdv.getEtudiant().getUniversite() != null) {
            etudiantUniversite = new UniversiteResponse(
                    rdv.getEtudiant().getUniversite().getId(),
                    rdv.getEtudiant().getUniversite().getNom(),
                    rdv.getEtudiant().getUniversite().getVille(),
                    rdv.getEtudiant().getUniversite().getAdresse(),
                    rdv.getEtudiant().getUniversite().getTelephone(),
                    rdv.getEtudiant().getUniversite().getNbEtudiants(),
                    rdv.getEtudiant().getUniversite().getHoraire(),
                    rdv.getEtudiant().getUniversite().getLogoPath(),
                    rdv.getEtudiant().getUniversite().getCode()
            );
        }

        // Create FULL EtudiantResponse
        EtudiantResponse etudiantResponse = new EtudiantResponse(
                rdv.getEtudiant().getId(),
                rdv.getEtudiant().getNom(),
                rdv.getEtudiant().getPrenom(),
                rdv.getEtudiant().getEmail(),
                rdv.getEtudiant().getTelephone(),
                rdv.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                etudiantUniversite
        );

        return new RdvResponse(
                rdv.getId(),
                rdv.getDate(),
                rdv.getHeure(),
                rdv.getStatus() != null ? rdv.getStatus().name() : "CONFIRMED",
                medecinResponse,
                etudiantResponse
        );
    }
    @Transactional
    public Rdv updateRdv(String id, RdvUpdateRequest request) {
        Rdv rdv = rdvRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé avec l'ID : " + id));

        if (request.getDate() != null) {
            rdv.setDate(request.getDate());
        }
        if (request.getHeure() != null) {
            rdv.setHeure(request.getHeure());
        }
        if (request.getStatus() != null) {
            try {
                rdv.setStatus(RdvStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Statut invalide. Valeurs acceptées: CONFIRMED, CANCELED");
            }
        }

        return rdvRepository.save(rdv);
    }

    @Transactional
    public void deleteRdv(String id) {
        if (!rdvRepository.existsById(id)) {
            throw new RuntimeException("Rendez-vous non trouvé avec l'ID : " + id);
        }
        rdvRepository.deleteById(id);
    }

    public List<RdvResponse> getRdvsByUniversiteId(Long universiteId) {
        // Verify university exists
        Universite universite = universiteRepository.findById(universiteId)
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + universiteId));

        List<Rdv> rdvs = rdvRepository.findByUniversiteId(universiteId);

        return rdvs.stream().map(r -> {
            // Convert doctor universities
            List<UniversiteResponse> medecinUniversites = r.getMedecin().getUniversites().stream()
                    .map(u -> new UniversiteResponse(
                            u.getId(),
                            u.getNom(),
                            u.getVille(),
                            u.getAdresse(),
                            u.getTelephone(),
                            u.getNbEtudiants(),
                            u.getHoraire(),
                            u.getLogoPath(),
                            u.getCode()
                    ))
                    .toList();

            // Create FULL MedecinResponse
            MedecinResponse medecinResponse = new MedecinResponse(
                    r.getMedecin().getId(),
                    r.getMedecin().getNom(),
                    r.getMedecin().getPrenom(),
                    r.getMedecin().getEmail(),
                    r.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                    r.getMedecin().getTelephone(),
                    medecinUniversites,
                    List.of(),
                    List.of()
            );

            // Convert student university
            UniversiteResponse etudiantUniversite = null;
            if (r.getEtudiant() != null && r.getEtudiant().getUniversite() != null) {
                etudiantUniversite = new UniversiteResponse(
                        r.getEtudiant().getUniversite().getId(),
                        r.getEtudiant().getUniversite().getNom(),
                        r.getEtudiant().getUniversite().getVille(),
                        r.getEtudiant().getUniversite().getAdresse(),
                        r.getEtudiant().getUniversite().getTelephone(),
                        r.getEtudiant().getUniversite().getNbEtudiants(),
                        r.getEtudiant().getUniversite().getHoraire(),
                        r.getEtudiant().getUniversite().getLogoPath(),
                        r.getEtudiant().getUniversite().getCode()
                );
            }

            // Create FULL EtudiantResponse
            EtudiantResponse etudiantResponse = new EtudiantResponse(
                    r.getEtudiant().getId(),
                    r.getEtudiant().getNom(),
                    r.getEtudiant().getPrenom(),
                    r.getEtudiant().getEmail(),
                    r.getEtudiant().getTelephone(),
                    r.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                    etudiantUniversite
            );

            return new RdvResponse(
                    r.getId(),
                    r.getDate(),
                    r.getHeure(),
                    r.getStatus() != null ? r.getStatus().name() : "CONFIRMED",
                    medecinResponse,
                    etudiantResponse
            );
        }).toList();
    }

    public List<RdvResponse> getRdvsByMedecinId(String medecinId) {
        // Verify medecin exists
        User medecin = userRepository.findById(medecinId)
                .filter(user -> user.getRole() == Role.MEDECIN)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé : " + medecinId));

        List<Rdv> rdvs = rdvRepository.findByMedecin_Id(medecinId);

        return rdvs.stream().map(r -> {
            // Convert doctor universities
            List<UniversiteResponse> medecinUniversites = r.getMedecin().getUniversites().stream()
                    .map(u -> new UniversiteResponse(
                            u.getId(),
                            u.getNom(),
                            u.getVille(),
                            u.getAdresse(),
                            u.getTelephone(),
                            u.getNbEtudiants(),
                            u.getHoraire(),
                            u.getLogoPath(),
                            u.getCode()
                    ))
                    .toList();

            // Create FULL MedecinResponse
            MedecinResponse medecinResponse = new MedecinResponse(
                    r.getMedecin().getId(),
                    r.getMedecin().getNom(),
                    r.getMedecin().getPrenom(),
                    r.getMedecin().getEmail(),
                    r.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                    r.getMedecin().getTelephone(),
                    medecinUniversites,
                    List.of(),
                    List.of()
            );

            // Convert student university
            UniversiteResponse etudiantUniversite = null;
            if (r.getEtudiant() != null && r.getEtudiant().getUniversite() != null) {
                etudiantUniversite = new UniversiteResponse(
                        r.getEtudiant().getUniversite().getId(),
                        r.getEtudiant().getUniversite().getNom(),
                        r.getEtudiant().getUniversite().getVille(),
                        r.getEtudiant().getUniversite().getAdresse(),
                        r.getEtudiant().getUniversite().getTelephone(),
                        r.getEtudiant().getUniversite().getNbEtudiants(),
                        r.getEtudiant().getUniversite().getHoraire(),
                        r.getEtudiant().getUniversite().getLogoPath(),
                        r.getEtudiant().getUniversite().getCode()
                );
            }

            // Create FULL EtudiantResponse
            EtudiantResponse etudiantResponse = new EtudiantResponse(
                    r.getEtudiant().getId(),
                    r.getEtudiant().getNom(),
                    r.getEtudiant().getPrenom(),
                    r.getEtudiant().getEmail(),
                    r.getEtudiant().getTelephone(),
                    r.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                    etudiantUniversite
            );

            return new RdvResponse(
                    r.getId(),
                    r.getDate(),
                    r.getHeure(),
                    r.getStatus() != null ? r.getStatus().name() : "CONFIRMED",
                    medecinResponse,
                    etudiantResponse
            );
        }).toList();
    }

    public List<RdvResponse> getRdvsByEtudiantId(String etudiantId) {
        // Verify etudiant exists
        User etudiant = userRepository.findById(etudiantId)
                .filter(user -> user.getRole() == Role.ETUDIANT)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé : " + etudiantId));

        List<Rdv> rdvs = rdvRepository.findByEtudiant_Id(etudiantId);

        return rdvs.stream().map(r -> {
            // Convert doctor universities
            List<UniversiteResponse> medecinUniversites = r.getMedecin().getUniversites().stream()
                    .map(u -> new UniversiteResponse(
                            u.getId(),
                            u.getNom(),
                            u.getVille(),
                            u.getAdresse(),
                            u.getTelephone(),
                            u.getNbEtudiants(),
                            u.getHoraire(),
                            u.getLogoPath(),
                            u.getCode()
                    ))
                    .toList();

            // Create FULL MedecinResponse
            MedecinResponse medecinResponse = new MedecinResponse(
                    r.getMedecin().getId(),
                    r.getMedecin().getNom(),
                    r.getMedecin().getPrenom(),
                    r.getMedecin().getEmail(),
                    r.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                    r.getMedecin().getTelephone(),
                    medecinUniversites,
                    List.of(),
                    List.of()
            );

            // Convert student university
            UniversiteResponse etudiantUniversite = null;
            if (r.getEtudiant() != null && r.getEtudiant().getUniversite() != null) {
                etudiantUniversite = new UniversiteResponse(
                        r.getEtudiant().getUniversite().getId(),
                        r.getEtudiant().getUniversite().getNom(),
                        r.getEtudiant().getUniversite().getVille(),
                        r.getEtudiant().getUniversite().getAdresse(),
                        r.getEtudiant().getUniversite().getTelephone(),
                        r.getEtudiant().getUniversite().getNbEtudiants(),
                        r.getEtudiant().getUniversite().getHoraire(),
                        r.getEtudiant().getUniversite().getLogoPath(),
                        r.getEtudiant().getUniversite().getCode()
                );
            }

            // Create FULL EtudiantResponse
            EtudiantResponse etudiantResponse = new EtudiantResponse(
                    r.getEtudiant().getId(),
                    r.getEtudiant().getNom(),
                    r.getEtudiant().getPrenom(),
                    r.getEtudiant().getEmail(),
                    r.getEtudiant().getTelephone(),
                    r.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                    etudiantUniversite
            );

            return new RdvResponse(
                    r.getId(),
                    r.getDate(),
                    r.getHeure(),
                    r.getStatus() != null ? r.getStatus().name() : "CONFIRMED",
                    medecinResponse,
                    etudiantResponse
            );
        }).toList();
    }

    public List<RdvResponse> getRdvsByStatus(String status) {
        try {
            RdvStatus rdvStatus = RdvStatus.valueOf(status);
            List<Rdv> rdvs = rdvRepository.findByStatus(rdvStatus);

            return rdvs.stream().map(r -> {
                // Convert doctor universities
                List<UniversiteResponse> medecinUniversites = r.getMedecin().getUniversites().stream()
                        .map(u -> new UniversiteResponse(
                                u.getId(),
                                u.getNom(),
                                u.getVille(),
                                u.getAdresse(),
                                u.getTelephone(),
                                u.getNbEtudiants(),
                                u.getHoraire(),
                                u.getLogoPath(),
                                u.getCode()
                        ))
                        .toList();

                // Create FULL MedecinResponse
                MedecinResponse medecinResponse = new MedecinResponse(
                        r.getMedecin().getId(),
                        r.getMedecin().getNom(),
                        r.getMedecin().getPrenom(),
                        r.getMedecin().getEmail(),
                        r.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                        r.getMedecin().getTelephone(),
                        medecinUniversites,
                        List.of(),
                        List.of()
                );

                // Convert student university
                UniversiteResponse etudiantUniversite = null;
                if (r.getEtudiant() != null && r.getEtudiant().getUniversite() != null) {
                    etudiantUniversite = new UniversiteResponse(
                            r.getEtudiant().getUniversite().getId(),
                            r.getEtudiant().getUniversite().getNom(),
                            r.getEtudiant().getUniversite().getVille(),
                            r.getEtudiant().getUniversite().getAdresse(),
                            r.getEtudiant().getUniversite().getTelephone(),
                            r.getEtudiant().getUniversite().getNbEtudiants(),
                            r.getEtudiant().getUniversite().getHoraire(),
                            r.getEtudiant().getUniversite().getLogoPath(),
                            r.getEtudiant().getUniversite().getCode()
                    );
                }

                // Create FULL EtudiantResponse
                EtudiantResponse etudiantResponse = new EtudiantResponse(
                        r.getEtudiant().getId(),
                        r.getEtudiant().getNom(),
                        r.getEtudiant().getPrenom(),
                        r.getEtudiant().getEmail(),
                        r.getEtudiant().getTelephone(),
                        r.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                        etudiantUniversite
                );

                return new RdvResponse(
                        r.getId(),
                        r.getDate(),
                        r.getHeure(),
                        r.getStatus() != null ? r.getStatus().name() : "CONFIRMED",
                        medecinResponse,
                        etudiantResponse
                );
            }).toList();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut invalide. Valeurs acceptées: CONFIRMED, CANCELED");
        }
    }
}