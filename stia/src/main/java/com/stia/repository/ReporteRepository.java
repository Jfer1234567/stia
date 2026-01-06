package com.stia.repository;

import com.stia.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    // Para que el Padre vea sus reportes (buscando por su correo/username)
    List<Reporte> findByEmailDestino(String emailDestino);
}