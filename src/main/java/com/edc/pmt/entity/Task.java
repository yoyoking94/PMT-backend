package com.edc.pmt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * Entité représentant une tâche dans un projet.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "project", "assignedTo" })
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "La date d'échéance est obligatoire")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotBlank(message = "La priorité est obligatoire")
    @Column(nullable = false, length = 10)
    private String priority;

    @NotBlank(message = "Le status est obligatoire")
    @Column(nullable = false, length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "tasks" })
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User assignedTo;
}
