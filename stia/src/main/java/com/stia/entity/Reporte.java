package com.stia.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Reporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Usuario estudiante;

    private String emailDestino; // El correo del padre al que se envió

    @Column(columnDefinition = "TEXT")
    private String contenido; // El resumen de notas que se envió

    private LocalDateTime fechaEnvio;
}