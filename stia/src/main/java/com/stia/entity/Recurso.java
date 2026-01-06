package com.stia.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Recurso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo; // Ej: "Aprende a sumar fracciones"
    private String url;    // Ej: Link de YouTube o PDF
    private String curso;  // Ej: "Matemática"
    private String nivel;  // Ej: "BASICO", "INTERMEDIO", "AVANZADO"
}