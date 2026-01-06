package com.stia.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Recomendacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String mensaje; // "Juan, mejora tus fracciones..."

    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "profesor_id")
    private Usuario profesor;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Usuario estudiante;
}