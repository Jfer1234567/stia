package com.stia.repository;

import com.stia.entity.Entrega;
import com.stia.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional; // <--- Importante

import java.util.List;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    // Agregamos esta anotación para que Thymeleaf pueda leer los PDFs sin romper Postgres
    @Transactional(readOnly = true)
    List<Entrega> findByEstudiante(Usuario estudiante);
}