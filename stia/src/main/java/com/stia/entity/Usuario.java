package com.stia.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "usuarios") // 'user' es palabra reservada en Postgres, mejor usar 'usuarios'
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // Será el email o nombre de usuario

    @Column(nullable = false)
    private String password; // Se guardará ENCRIPTADA

    @Enumerated(EnumType.STRING)
    private Rol rol; // ESTUDIANTE, DOCENTE, PADRE

    private String nombreCompleto;
    private String email;
    private String emailPadre;


}