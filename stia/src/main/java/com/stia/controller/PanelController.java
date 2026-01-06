package com.stia.controller;

import com.stia.entity.*;
import com.stia.repository.*;
import com.stia.service.NotificacionService;
import com.stia.service.StiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PanelController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EntregaRepository entregaRepository;
    @Autowired private CalificacionRepository calificacionRepository;
    @Autowired private RecomendacionRepository recomendacionRepository;
    @Autowired private StiaService stiaService;

    @Autowired private ReporteRepository reporteRepository;
    @Autowired private NotificacionService notificacionService;
    @Autowired private RecursoRepository recursoRepository;

    // --- PÁGINA PRINCIPAL (DASHBOARD) ---
    @GetMapping("/")
    @Transactional(readOnly = true)
    public String index(Model model) {
        // 1. OBTENER USUARIO DESDE SPRING SECURITY (Ya autenticado)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // 2. BUSCAR DATOS (Spring Security garantiza que el usuario existe, usamos .get())
        Usuario usuario = usuarioRepository.findByUsername(username).get();
        model.addAttribute("usuario", usuario);

        // ==========================================
        // LÓGICA PARA EL PROFESOR
        // ==========================================
        if (usuario.getRol() == Rol.DOCENTE) {
            List<Usuario> alumnos = usuarioRepository.findByRol(Rol.ESTUDIANTE);
            model.addAttribute("alumnos", alumnos);

            Map<Long, List<Entrega>> mapaEntregas = new HashMap<>();
            Map<Long, List<Calificacion>> mapaNotas = new HashMap<>();

            for (Usuario al : alumnos) {
                mapaEntregas.put(al.getId(), entregaRepository.findByEstudiante(al));
                mapaNotas.put(al.getId(), calificacionRepository.findByEstudiante(al));
            }

            model.addAttribute("mapaEntregas", mapaEntregas);
            model.addAttribute("mapaNotas", mapaNotas);
        }

        // ==========================================
        // LÓGICA PARA EL ALUMNO (Con Recomendación Inteligente)
        // ==========================================
        if (usuario.getRol() == Rol.ESTUDIANTE) {
            List<Calificacion> historialNotas = calificacionRepository.findByEstudiante(usuario);
            model.addAttribute("misNotas", historialNotas);
            model.addAttribute("misEntregas", entregaRepository.findByEstudiante(usuario));
            model.addAttribute("misRecomendaciones", recomendacionRepository.findByEstudiante(usuario));

            // FILTRADO INTELIGENTE: Solo última nota por curso
            Map<String, Calificacion> ultimasNotasMap = new HashMap<>();
            for (Calificacion cal : historialNotas) {
                if (!ultimasNotasMap.containsKey(cal.getCurso()) || cal.getId() > ultimasNotasMap.get(cal.getCurso()).getId()) {
                    ultimasNotasMap.put(cal.getCurso(), cal);
                }
            }

            // GENERAR RECURSOS
            List<Recurso> recursosSugeridos = new ArrayList<>();
            for (Calificacion cal : ultimasNotasMap.values()) {
                String nivelNecesario = "BASICO";
                if (cal.getNota() >= 11 && cal.getNota() <= 15) {
                    nivelNecesario = "INTERMEDIO";
                } else if (cal.getNota() > 15) {
                    nivelNecesario = "AVANZADO";
                }
                List<Recurso> encontrados = recursoRepository.findByCursoAndNivel(cal.getCurso(), nivelNecesario);
                recursosSugeridos.addAll(encontrados);
            }
            model.addAttribute("recursosSugeridos", recursosSugeridos);
        }

        // ==========================================
        // LÓGICA PARA EL PADRE
        // ==========================================
        if (usuario.getRol() == Rol.PADRE) {
            String emailBusqueda = usuario.getEmail();
            List<Reporte> misReportes = reporteRepository.findByEmailDestino(emailBusqueda);
            model.addAttribute("misReportes", misReportes);
        }

        return "index";
    }

    // --- ACCIONES DEL SISTEMA ---

    @PostMapping("/alumno/entregar")
    public String subirEntrega(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario alumno = usuarioRepository.findByUsername(auth.getName()).get();

        String textoPdf = stiaService.extraerTextoDesdePdf(archivo);

        Entrega entrega = new Entrega();
        entrega.setNombreArchivo(archivo.getOriginalFilename());
        entrega.setContenidoTexto(textoPdf);
        entrega.setDatosArchivo(archivo.getBytes());
        entrega.setFechaEntrega(LocalDateTime.now());
        entrega.setEstudiante(alumno);

        entregaRepository.save(entrega);
        return "redirect:/?exito=entregado";
    }

    @PostMapping("/profesor/calificar")
    public String calificar(@RequestParam Long estudianteId, @RequestParam Double nota, @RequestParam String curso) {
        Usuario alumno = usuarioRepository.findById(estudianteId).get();

        Calificacion cal = new Calificacion();
        cal.setEstudiante(alumno);
        cal.setNota(nota);
        cal.setCurso(curso);

        calificacionRepository.save(cal);
        return "redirect:/?exito=calificado";
    }

    @PostMapping("/profesor/recomendar")
    public String recomendar(@RequestParam Long estudianteId, @RequestParam String mensaje) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario profe = usuarioRepository.findByUsername(auth.getName()).get();
        Usuario alumno = usuarioRepository.findById(estudianteId).get();

        Recomendacion rec = new Recomendacion();
        rec.setProfesor(profe);
        rec.setEstudiante(alumno);
        rec.setMensaje(mensaje);
        rec.setFecha(LocalDateTime.now());

        recomendacionRepository.save(rec);
        return "redirect:/?exito=recomendado";
    }

    @GetMapping("/entrega/descargar/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        Entrega entrega = entregaRepository.findById(id).orElseThrow();

        if (entrega.getDatosArchivo() == null || entrega.getDatosArchivo().length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + entrega.getNombreArchivo() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(entrega.getDatosArchivo());
    }

    @GetMapping("/profesor/analizar-tarea/{id}")
    @ResponseBody
    public Map<String, String> analizarTarea(@PathVariable Long id) {
        Entrega entrega = entregaRepository.findById(id).orElseThrow();
        String prompt = "Actúa como un profesor auxiliar. Analiza esta tarea:\n" +
                "CONTENIDO: " + entrega.getContenidoTexto() + "\n\n" +
                "Dime: 1) De qué trata. 2) Si parece completa. 3) Sugerencia de nota. Sé breve.";
        String analisis = stiaService.consultarTutor(prompt);
        return Map.of("analisis", analisis);
    }

    @PostMapping("/alumno/chat-tutor")
    @ResponseBody
    public Map<String, String> chatTutor(@RequestBody Map<String, String> payload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario alumno = usuarioRepository.findByUsername(auth.getName()).get();
        String pregunta = payload.get("pregunta");
        String respuesta = stiaService.generarRespuestaAdaptativa(alumno, pregunta);
        return Map.of("respuesta", respuesta);
    }

    @PostMapping("/profesor/enviar-reporte")
    public String enviarReportePadres(@RequestParam Long estudianteId) {
        Usuario alumno = usuarioRepository.findById(estudianteId).orElseThrow();
        notificacionService.generarYEnviarReporte(alumno);
        return "redirect:/?exito=reporte_enviado";
    }
}