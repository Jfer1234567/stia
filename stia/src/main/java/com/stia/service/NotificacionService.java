package com.stia.service;

import com.stia.entity.*;
import com.stia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    @Autowired private CalificacionRepository calificacionRepository;
    @Autowired private ReporteRepository reporteRepository;

    // Inyectamos el enviador de correos de Spring (puede ser nulo si no está configurado)
    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void generarYEnviarReporte(Usuario alumno) {
        // 1. Obtener notas
        List<Calificacion> notas = calificacionRepository.findByEstudiante(alumno);

        // 2. Construir el mensaje
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Estimado Padre/Apoderado,\n\n");
        cuerpo.append("Aquí está el reporte de calificaciones de su hijo(a) ").append(alumno.getUsername()).append(":\n");
        cuerpo.append("--------------------------------------------------\n");

        double suma = 0;
        for (Calificacion c : notas) {
            cuerpo.append(String.format("- %s: %.2f\n", c.getCurso(), c.getNota()));
            suma += c.getNota();
        }
        double promedio = notas.isEmpty() ? 0 : suma / notas.size();

        cuerpo.append("--------------------------------------------------\n");
        cuerpo.append(String.format("PROMEDIO GENERAL: %.2f\n", promedio));

        if(promedio < 11) cuerpo.append("⚠️ ESTADO: EN RIESGO. Se requiere atención inmediata.\n");
        else cuerpo.append("✅ ESTADO: APROBADO. ¡Buen trabajo!\n");

        // 3. Guardar Historial en Base de Datos (Para el Dashboard del Padre)
        Reporte reporte = new Reporte();
        reporte.setEstudiante(alumno);
        reporte.setContenido(cuerpo.toString());
        reporte.setFechaEnvio(LocalDateTime.now());
        // Asumimos que el email del padre está guardado en el alumno,
        // o usamos un valor por defecto para pruebas si está vacío.
        String emailDestino = (alumno.getEmailPadre() != null && !alumno.getEmailPadre().isEmpty())
                ? alumno.getEmailPadre() : "padre@ejemplo.com";
        reporte.setEmailDestino(emailDestino);

        reporteRepository.save(reporte);

        // 4. Intentar Enviar Correo Real (Si falla, solo lo logueamos)
        try {
            if (mailSender != null) {
                SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(emailDestino);
                email.setSubject("Reporte Académico STIA: " + alumno.getUsername());
                email.setText(cuerpo.toString());
                mailSender.send(email);
                System.out.println("📧 Correo enviado a: " + emailDestino);
            } else {
                System.out.println("⚠️ JavaMailSender no configurado. Solo se guardó en BD.");
            }
        } catch (Exception e) {
            System.out.println("❌ Error enviando correo (puede ser configuración SMTP): " + e.getMessage());
        }
    }
}