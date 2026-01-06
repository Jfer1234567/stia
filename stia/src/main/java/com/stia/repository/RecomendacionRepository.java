package com.stia.repository;
import com.stia.entity.Recomendacion;
import com.stia.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {
    List<Recomendacion> findByEstudiante(Usuario estudiante);
}