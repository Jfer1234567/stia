package com.stia.repository;

import com.stia.entity.Recurso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecursoRepository extends JpaRepository<Recurso, Long> {
    // Método mágico para encontrar materiales específicos
    List<Recurso> findByCursoAndNivel(String curso, String nivel);
}