package com.stia.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Calificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String curso; // Ej: Matemáticas
    private Double nota;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Usuario estudiante;
}