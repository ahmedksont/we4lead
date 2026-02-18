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

        // Nouveaux champs pour le médecin
        user.setSpecialite(request.getSpecialite());
        user.setGenre(request.getGenre());
        user.setSituation(request.getSituation());

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

            List<RdvResponse> rdvs = rdvRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(r -> {
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
                                r.getMedecin().getSpecialite(), // Ajout de la spécialité
                                medecinUniversites,
                                List.of(),
                                List.of()
                        );

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
                                        etudiantUniversite,
                                        r.getEtudiant().getGenre(),      // Ajout du genre
                                        r.getEtudiant().getSituation(),   // Ajout de la situation
                                        r.getEtudiant().getNiveauEtude()  // Ajout du niveau d'étude
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
                    m.getSpecialite(), // Ajout de la spécialité
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
        // Nouveaux champs pour le médecin
        if (request.getSpecialite() != null) {
            user.setSpecialite(request.getSpecialite());
        }
        if (request.getGenre() != null) {
            user.setGenre(request.getGenre());
        }
        if (request.getSituation() != null) {
            user.setSituation(request.getSituation());
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

        long appointmentsCount = rdvRepository.countByMedecin_Id(id);

        if (appointmentsCount > 0 && !forceCascade) {
            throw new RuntimeException("Ce médecin a " + appointmentsCount + " rendez-vous. Utilisez forceCascade=true pour supprimer quand même.");
        }

        if (forceCascade) {
            rdvRepository.deleteByMedecin_Id(id);
            creneauRepository.deleteByMedecin_Id(id);
        }

        userRepository.delete(medecin);
    }

    public List<MedecinResponse> getMedecinsByUniversiteId(Long universiteId) {
        Universite universite = universiteRepository.findById(universiteId)
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + universiteId));

        List<User> medecins = userRepository.findByUniversiteIdAndRole(universiteId, Role.MEDECIN);

        return medecins.stream().map(m -> {
            List<CreneauResponse> creneaux = creneauRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(c -> new CreneauResponse(c.getId(), c.getJour(), c.getDebut(), c.getFin()))
                    .toList();

            List<RdvResponse> rdvs = rdvRepository.findByMedecin_Id(m.getId())
                    .stream()
                    .map(r -> {
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
                                r.getMedecin().getSpecialite(), // Ajout de la spécialité
                                medecinUniversites,
                                List.of(),
                                List.of()
                        );

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
                                        etudiantUniversite,
                                        r.getEtudiant().getGenre(),      // Ajout du genre
                                        r.getEtudiant().getSituation(),   // Ajout de la situation
                                        r.getEtudiant().getNiveauEtude()  // Ajout du niveau d'étude
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
                    m.getSpecialite(), // Ajout de la spécialité
                    universiteResponses,
                    creneaux,
                    rdvs
            );
        }).toList();
    }

    @Transactional
    public User createEtudiant(UserCreateRequest request) {
        if (request.getUniversiteId() == null) {
            throw new IllegalArgumentException("L'université est obligatoire pour créer un étudiant");
        }

        Universite universite = universiteRepository.findById(request.getUniversiteId())
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + request.getUniversiteId()));

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());
        user.setRole(Role.ETUDIANT);

        // Nouveaux champs pour l'étudiant
        user.setGenre(request.getGenre());
        user.setSituation(request.getSituation());
        user.setNiveauEtude(request.getNiveauEtude());

        user.setUniversite(universite);

        User savedUser = userRepository.save(user);

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
                    universiteResponse,
                    e.getGenre(),        // Ajout du genre
                    e.getSituation(),     // Ajout de la situation
                    e.getNiveauEtude()    // Ajout du niveau d'étude
            );
        }).toList();
    }

    public List<EtudiantResponse> getEtudiantsByUniversiteId(Long universiteId) {
        Universite universite = universiteRepository.findById(universiteId)
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + universiteId));

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
                    universiteResponse,
                    e.getGenre(),        // Ajout du genre
                    e.getSituation(),     // Ajout de la situation
                    e.getNiveauEtude()    // Ajout du niveau d'étude
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
        // Nouveaux champs pour l'étudiant
        if (request.getGenre() != null) {
            user.setGenre(request.getGenre());
        }
        if (request.getSituation() != null) {
            user.setSituation(request.getSituation());
        }
        if (request.getNiveauEtude() != null) {
            user.setNiveauEtude(request.getNiveauEtude());
        }

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

        long appointmentsCount = rdvRepository.countByEtudiant_Id(id);

        if (appointmentsCount > 0) {
            rdvRepository.deleteByEtudiant_Id(id);
        }

        userRepository.delete(etudiant);
    }

    // ================= RDV (APPOINTMENTS) CRUD =================

    @Transactional
    public Rdv createRdv(RdvRequest request) {
        if (request.getMedecinId() == null) {
            throw new IllegalArgumentException("Médecin est obligatoire");
        }
        if (request.getEtudiantId() == null) {
            throw new IllegalArgumentException("Étudiant est obligatoire");
        }
        if (request.getDate() == null || request.getHeure() == null) {
            throw new IllegalArgumentException("Date et heure sont obligatoires");
        }

        User medecin = userRepository.findById(request.getMedecinId())
                .filter(user -> user.getRole() == Role.MEDECIN)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé : " + request.getMedecinId()));

        User etudiant = userRepository.findById(request.getEtudiantId())
                .filter(user -> user.getRole() == Role.ETUDIANT)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé : " + request.getEtudiantId()));

        boolean sameUniversity = medecin.getUniversites().stream()
                .anyMatch(u -> u.getId().equals(etudiant.getUniversite().getId()));

        if (!sameUniversity) {
            throw new IllegalArgumentException("L'étudiant et le médecin doivent appartenir à la même université");
        }

        boolean slotTaken = rdvRepository.existsByMedecin_IdAndDateAndHeure(
                request.getMedecinId(),
                request.getDate(),
                request.getHeure()
        );

        if (slotTaken) {
            throw new IllegalArgumentException("Ce créneau est déjà réservé pour ce médecin");
        }

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

            MedecinResponse medecinResponse = new MedecinResponse(
                    r.getMedecin().getId(),
                    r.getMedecin().getNom(),
                    r.getMedecin().getPrenom(),
                    r.getMedecin().getEmail(),
                    r.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                    r.getMedecin().getTelephone(),
                    r.getMedecin().getSpecialite(), // Ajout de la spécialité
                    medecinUniversites,
                    List.of(),
                    List.of()
            );

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

            EtudiantResponse etudiantResponse = new EtudiantResponse(
                    r.getEtudiant().getId(),
                    r.getEtudiant().getNom(),
                    r.getEtudiant().getPrenom(),
                    r.getEtudiant().getEmail(),
                    r.getEtudiant().getTelephone(),
                    r.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                    etudiantUniversite,
                    r.getEtudiant().getGenre(),      // Ajout du genre
                    r.getEtudiant().getSituation(),   // Ajout de la situation
                    r.getEtudiant().getNiveauEtude()  // Ajout du niveau d'étude
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

        MedecinResponse medecinResponse = new MedecinResponse(
                rdv.getMedecin().getId(),
                rdv.getMedecin().getNom(),
                rdv.getMedecin().getPrenom(),
                rdv.getMedecin().getEmail(),
                rdv.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                rdv.getMedecin().getTelephone(),
                rdv.getMedecin().getSpecialite(), // Ajout de la spécialité
                medecinUniversites,
                List.of(),
                List.of()
        );

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

        EtudiantResponse etudiantResponse = new EtudiantResponse(
                rdv.getEtudiant().getId(),
                rdv.getEtudiant().getNom(),
                rdv.getEtudiant().getPrenom(),
                rdv.getEtudiant().getEmail(),
                rdv.getEtudiant().getTelephone(),
                rdv.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                etudiantUniversite,
                rdv.getEtudiant().getGenre(),      // Ajout du genre
                rdv.getEtudiant().getSituation(),   // Ajout de la situation
                rdv.getEtudiant().getNiveauEtude()  // Ajout du niveau d'étude
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
        Universite universite = universiteRepository.findById(universiteId)
                .orElseThrow(() -> new IllegalArgumentException("Université non trouvée : " + universiteId));

        List<Rdv> rdvs = rdvRepository.findByUniversiteId(universiteId);

        return rdvs.stream().map(r -> {
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

            MedecinResponse medecinResponse = new MedecinResponse(
                    r.getMedecin().getId(),
                    r.getMedecin().getNom(),
                    r.getMedecin().getPrenom(),
                    r.getMedecin().getEmail(),
                    r.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                    r.getMedecin().getTelephone(),
                    r.getMedecin().getSpecialite(), // Ajout de la spécialité
                    medecinUniversites,
                    List.of(),
                    List.of()
            );

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

            EtudiantResponse etudiantResponse = new EtudiantResponse(
                    r.getEtudiant().getId(),
                    r.getEtudiant().getNom(),
                    r.getEtudiant().getPrenom(),
                    r.getEtudiant().getEmail(),
                    r.getEtudiant().getTelephone(),
                    r.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                    etudiantUniversite,
                    r.getEtudiant().getGenre(),      // Ajout du genre
                    r.getEtudiant().getSituation(),   // Ajout de la situation
                    r.getEtudiant().getNiveauEtude()  // Ajout du niveau d'étude
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
        User medecin = userRepository.findById(medecinId)
                .filter(user -> user.getRole() == Role.MEDECIN)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé : " + medecinId));

        List<Rdv> rdvs = rdvRepository.findByMedecin_Id(medecinId);

        return rdvs.stream().map(r -> {
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

            MedecinResponse medecinResponse = new MedecinResponse(
                    r.getMedecin().getId(),
                    r.getMedecin().getNom(),
                    r.getMedecin().getPrenom(),
                    r.getMedecin().getEmail(),
                    r.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                    r.getMedecin().getTelephone(),
                    r.getMedecin().getSpecialite(), // Ajout de la spécialité
                    medecinUniversites,
                    List.of(),
                    List.of()
            );

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

            EtudiantResponse etudiantResponse = new EtudiantResponse(
                    r.getEtudiant().getId(),
                    r.getEtudiant().getNom(),
                    r.getEtudiant().getPrenom(),
                    r.getEtudiant().getEmail(),
                    r.getEtudiant().getTelephone(),
                    r.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                    etudiantUniversite,
                    r.getEtudiant().getGenre(),      // Ajout du genre
                    r.getEtudiant().getSituation(),   // Ajout de la situation
                    r.getEtudiant().getNiveauEtude()  // Ajout du niveau d'étude
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
        User etudiant = userRepository.findById(etudiantId)
                .filter(user -> user.getRole() == Role.ETUDIANT)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé : " + etudiantId));

        List<Rdv> rdvs = rdvRepository.findByEtudiant_Id(etudiantId);

        return rdvs.stream().map(r -> {
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

            MedecinResponse medecinResponse = new MedecinResponse(
                    r.getMedecin().getId(),
                    r.getMedecin().getNom(),
                    r.getMedecin().getPrenom(),
                    r.getMedecin().getEmail(),
                    r.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                    r.getMedecin().getTelephone(),
                    r.getMedecin().getSpecialite(), // Ajout de la spécialité
                    medecinUniversites,
                    List.of(),
                    List.of()
            );

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

            EtudiantResponse etudiantResponse = new EtudiantResponse(
                    r.getEtudiant().getId(),
                    r.getEtudiant().getNom(),
                    r.getEtudiant().getPrenom(),
                    r.getEtudiant().getEmail(),
                    r.getEtudiant().getTelephone(),
                    r.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                    etudiantUniversite,
                    r.getEtudiant().getGenre(),      // Ajout du genre
                    r.getEtudiant().getSituation(),   // Ajout de la situation
                    r.getEtudiant().getNiveauEtude()  // Ajout du niveau d'étude
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

                MedecinResponse medecinResponse = new MedecinResponse(
                        r.getMedecin().getId(),
                        r.getMedecin().getNom(),
                        r.getMedecin().getPrenom(),
                        r.getMedecin().getEmail(),
                        r.getMedecin().getPhotoPath() != null ? "/users/me/photo" : null,
                        r.getMedecin().getTelephone(),
                        r.getMedecin().getSpecialite(), // Ajout de la spécialité
                        medecinUniversites,
                        List.of(),
                        List.of()
                );

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

                EtudiantResponse etudiantResponse = new EtudiantResponse(
                        r.getEtudiant().getId(),
                        r.getEtudiant().getNom(),
                        r.getEtudiant().getPrenom(),
                        r.getEtudiant().getEmail(),
                        r.getEtudiant().getTelephone(),
                        r.getEtudiant().getPhotoPath() != null ? "/users/me/photo" : null,
                        etudiantUniversite,
                        r.getEtudiant().getGenre(),      // Ajout du genre
                        r.getEtudiant().getSituation(),   // Ajout de la situation
                        r.getEtudiant().getNiveauEtude()  // Ajout du niveau d'étude
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