package com.stia.repository;
import com.stia.entity.Calificacion;
import com.stia.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findByEstudiante(Usuario estudiante);
}