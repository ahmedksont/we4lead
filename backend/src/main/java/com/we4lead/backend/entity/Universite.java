package com.we4lead.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "universite")
public class Universite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String ville;
    @Column(unique = true)
    private String code;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Universite() {}

    public Universite(String nom, String ville) {
        this.nom = nom;
        this.ville = ville;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
}
