package com.stia.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Entrega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreArchivo;

    @Column(columnDefinition = "TEXT")
    private String contenidoTexto; // Para la IA

    // NUEVO: Guardamos el archivo físico para descargar
    @Lob
    @Column(length = 10000000) // Soporte hasta ~10MB
    private byte[] datosArchivo;

    private LocalDateTime fechaEntrega;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Usuario estudiante;
}