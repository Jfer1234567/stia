package com.stia.config;

import com.stia.entity.Recurso;
import com.stia.repository.RecursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private RecursoRepository recursoRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔄 Verificando Banco de Recursos...");

        // 1. MATEMÁTICA (Si no existen, los crea)
        if (recursoRepository.findByCursoAndNivel("Matemática", "BASICO").isEmpty()) {
            crearRecurso("Matemática", "BASICO", "Aprende a Sumar", "https://www.youtube.com/watch?v=o-jTS8K_K4E");
            crearRecurso("Matemática", "BASICO", "Tablas de Multiplicar", "https://www.youtube.com/watch?v=ecSMePh4Mzw");
            crearRecurso("Matemática", "INTERMEDIO", "Fracciones para Primaria", "https://www.youtube.com/watch?v=TV5nE13pXj8");
            crearRecurso("Matemática", "AVANZADO", "Ecuaciones de Primer Grado", "https://www.youtube.com/watch?v=CN4n6y7kXts");
            System.out.println("➕ Recursos de Matemática agregados.");
        }

        // 2. COMUNICACIÓN
        if (recursoRepository.findByCursoAndNivel("Comunicación", "BASICO").isEmpty()) {
            crearRecurso("Comunicación", "BASICO", "El Uso de las Mayúsculas", "https://www.youtube.com/watch?v=2d1k6d-v6a0");
            crearRecurso("Comunicación", "INTERMEDIO", "Técnicas de Lectura", "https://www.youtube.com/watch?v=6f8q6b0_w8s");
            crearRecurso("Comunicación", "AVANZADO", "El Sujeto y el Predicado", "https://www.youtube.com/watch?v=W9hvV0_oXvE");
            System.out.println("📖 Recursos de Comunicación agregados.");
        }

        // 3. PERSONAL SOCIAL (¡TUS NUEVOS RECURSOS!)
        if (recursoRepository.findByCursoAndNivel("Personal Social", "BASICO").isEmpty()) {
            crearRecurso("Personal Social", "BASICO", "Normas de Convivencia", "https://www.youtube.com/watch?v=wdDHuBMI2w0");
            crearRecurso("Personal Social", "BASICO", "Roles en la Familia", "https://www.youtube.com/watch?v=SnCR0GSC1vo");

            crearRecurso("Personal Social", "INTERMEDIO", "Derechos del Niño (UNICEF)", "https://www.youtube.com/watch?v=Mj3-0lQXWUo");
            crearRecurso("Personal Social", "INTERMEDIO", "Regiones del Perú", "https://www.youtube.com/watch?v=Qmi3tCFk5ec");

            crearRecurso("Personal Social", "AVANZADO", "El Imperio de los Incas", "https://www.youtube.com/watch?v=Y-6Jmf1Eq1g");
            crearRecurso("Personal Social", "AVANZADO", "Poderes del Estado Peruano", "https://www.youtube.com/watch?v=vyKGbOkuric");
            System.out.println("🤝 Recursos de Personal Social agregados.");
        }

        // 4. CIENCIA Y TECNOLOGÍA (Para completar el menú)
        if (recursoRepository.findByCursoAndNivel("Ciencia y Tecnología", "BASICO").isEmpty()) {
            crearRecurso("Ciencia y Tecnología", "BASICO", "Los 5 Sentidos", "https://www.youtube.com/watch?v=a_EfwFzm1ys");
            crearRecurso("Ciencia y Tecnología", "INTERMEDIO", "El Ciclo del Agua", "https://www.youtube.com/watch?v=yhTXJLXJYIQ");
            crearRecurso("Ciencia y Tecnología", "AVANZADO", "La Célula Animal y Vegetal", "https://www.youtube.com/watch?v=e9rsxJh4KIU");
            System.out.println("🔬 Recursos de Ciencia y Tecnología agregados.");
        }

        System.out.println("✅ Carga de datos finalizada.");
    }

    private void crearRecurso(String curso, String nivel, String titulo, String url) {
        Recurso r = new Recurso();
        r.setCurso(curso);
        r.setNivel(nivel);
        r.setTitulo(titulo);
        r.setUrl(url);
        recursoRepository.save(r);
    }
}