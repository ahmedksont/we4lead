package com.we4lead.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
public class User {

    @Id
    private String id;

    @Column(unique = true)
    private String email;

    private String nom;
    private String prenom;
    private String telephone;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String photoPath;

    // NOUVEAUX CHAMPS
    @Enumerated(EnumType.STRING)
    private Situation situation;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Column(name = "niveau_etude")
    private String niveauEtude;

    private String specialite;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "medecin_universite",
            joinColumns = @JoinColumn(name = "medecin_id"),
            inverseJoinColumns = @JoinColumn(name = "universite_id")
    )
    private Set<Universite> universites = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "universite_id")
    private Universite universite;

    public User() {}

    public User(String id, String email, String nom, String prenom, String telephone, Role role) {
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.role = role;
    }

    // ====== Getters & Setters existants ======
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public Set<Universite> getUniversites() { return universites; }
    public void setUniversites(Set<Universite> universites) { this.universites = universites; }

    public Universite getUniversite() { return universite; }
    public void setUniversite(Universite universite) { this.universite = universite; }

    // ====== Getters & Setters pour les nouveaux champs ======
    public Situation getSituation() { return situation; }
    public void setSituation(Situation situation) { this.situation = situation; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public String getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(String niveauEtude) { this.niveauEtude = niveauEtude; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
}