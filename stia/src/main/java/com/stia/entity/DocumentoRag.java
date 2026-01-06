package com.stia.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DocumentoRag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreArchivo;

    @Column(columnDefinition = "TEXT") // Permite guardar textos muy largos
    private String contenidoTexto;
}