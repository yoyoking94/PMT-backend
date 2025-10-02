package com.edc.pmt.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "MembreProjet")
public class MembreProjet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projetId;

    @Column(nullable = false)
    private Long utilisateurId;

    @Column(nullable = false)
    private String role; // "administrateur", "membre", etc.
};