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

    // For medecins - many-to-many with universities
    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "medecin_universite",
            joinColumns = @JoinColumn(name = "medecin_id"),
            inverseJoinColumns = @JoinColumn(name = "universite_id")
    )
    private Set<Universite> universites = new HashSet<>();

    // For admins - one-to-many relationship (an admin belongs to one university)
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

    // ====== Getters & Setters ======
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
}