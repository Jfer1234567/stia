package com.stia.service;

import com.stia.entity.*;
import com.stia.repository.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StiaService {

    @Autowired private DocumentoRepository documentoRepository;
    @Autowired private EntregaRepository entregaRepository;
    @Autowired private CalificacionRepository calificacionRepository;

    @Value("${groq.api.key}") private String apiKey;
    @Value("${groq.api.url}") private String apiUrl;
    @Value("${groq.api.model}") private String model;

    // --- 1. UTILIDADES (EXTRACCIÓN PDF) ---
    public String extraerTextoDesdePdf(MultipartFile archivo) throws IOException {
        try (PDDocument document = PDDocument.load(archivo.getInputStream())) {
            if (document.isEncrypted()) {
                return "Error: El PDF está protegido con contraseña y no se puede leer.";
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String texto = stripper.getText(document);

            // Validación: Si el texto está vacío, probablemente sea una imagen
            if (texto == null || texto.trim().isEmpty()) {
                return "[ALERTA] El PDF parece ser una imagen escaneada sin texto seleccionable.";
            }
            // Limpieza básica para ahorrar tokens
            return texto.replaceAll("\\s+", " ").trim();
        }
    }

    // --- 2. SUBIDA GENERAL (Para documentos del sistema) ---
    public String guardarConocimiento(MultipartFile archivo) throws IOException {
        String texto = extraerTextoDesdePdf(archivo);
        DocumentoRag doc = new DocumentoRag();
        doc.setNombreArchivo(archivo.getOriginalFilename());
        doc.setContenidoTexto(texto);
        documentoRepository.save(doc);
        return "He aprendido el contenido de: " + archivo.getOriginalFilename();
    }

    // --- 3. CEREBRO RAG + ADAPTATIVO (Lógica Principal) ---
    @Transactional(readOnly = true)
    public String generarRespuestaAdaptativa(Usuario alumno, String pregunta) {

        System.out.println("--- INICIANDO ANÁLISIS IA PARA: " + alumno.getUsername() + " ---");

        // A. OBTENER EL ÚLTIMO PDF REAL (Ordenado por fecha)
        List<Entrega> entregas = entregaRepository.findByEstudiante(alumno);

        String contextoEstudio = "No se encontró ningún documento subido recientemente.";
        String nombreArchivo = "Ninguno";

        if (!entregas.isEmpty()) {
            // Buscamos la entrega con la fecha más reciente
            Entrega ultima = entregas.stream()
                    .max(Comparator.comparing(Entrega::getFechaEntrega))
                    .orElse(entregas.get(entregas.size() - 1));

            nombreArchivo = ultima.getNombreArchivo();
            contextoEstudio = ultima.getContenidoTexto();

            // Cortar texto si es muy largo (Límite de tokens)
            if (contextoEstudio != null && contextoEstudio.length() > 15000) {
                contextoEstudio = contextoEstudio.substring(0, 15000) + "... [Texto cortado]";
            }
        }

        // B. CALCULAR PROMEDIO Y DEFINIR PERSONALIDAD
        List<Calificacion> notas = calificacionRepository.findByEstudiante(alumno);
        double promedio = notas.stream().mapToDouble(Calificacion::getNota).average().orElse(0.0);

        String instruccionesPersonalidad;

        if (promedio < 11) {
            instruccionesPersonalidad = "TU PERSONALIDAD: El alumno está EN RIESGO (Promedio " + String.format("%.1f", promedio) + "). " +
                    "Tu tono debe ser MUY EMPÁTICO, PACIENTE y SENCILLO. " +
                    "Explica paso a paso, usa analogías simples. Evita tecnicismos.";
        } else if (promedio > 16) {
            instruccionesPersonalidad = "TU PERSONALIDAD: El alumno es SOBRESALIENTE (Promedio " + String.format("%.1f", promedio) + "). " +
                    "Tu tono debe ser PROFESIONAL, RÁPIDO y TÉCNICO. " +
                    "No des explicaciones obvias. Ve al grano.";
        } else {
            instruccionesPersonalidad = "TU PERSONALIDAD: El alumno es REGULAR (Promedio " + String.format("%.1f", promedio) + "). " +
                    "Sé claro, directo y útil. Equilibra la teoría con ejemplos.";
        }

        // C. PROMPT MAESTRO
        String prompt = "Eres STIA, un tutor virtual inteligente.\n" +
                instruccionesPersonalidad + "\n\n" +
                "CONTEXTO DEL ARCHIVO ESTUDIADO:\n" +
                "- Archivo: " + nombreArchivo + "\n" +
                "- Contenido: " + contextoEstudio + "\n\n" +
                "PREGUNTA DEL ALUMNO: \"" + pregunta + "\"\n\n" +
                "INSTRUCCIONES:\n" +
                "1. Responde basándote PRINCIPALMENTE en el contenido del archivo si es relevante.\n" +
                "2. Si el archivo dice '[ALERTA]', avisa amablemente que no pudiste leer el PDF.\n" +
                "3. Mantén tu respuesta concisa.";

        return llamarGroq(prompt);
    }

    // Método simple para pruebas o uso del profesor
    public String consultarTutor(String prompt) {
        return llamarGroq(prompt);
    }

    // --- 4. CONEXIÓN CON GROQ (SOLUCIÓN HANDSHAKE) ---
    private String llamarGroq(String prompt) {
        try {
            // 1. Configurar Timeout (Paciencia de conexión)
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(20000); // 20 seg
            factory.setReadTimeout(20000);    // 20 seg
            RestTemplate restTemplate = new RestTemplate(factory);

            // 2. Cabeceras (AQUÍ ESTÁ LA SOLUCIÓN DEL HANDSHAKE)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // ---> ESTA LÍNEA EVITA QUE EL SERVIDOR CORTE LA CONEXIÓN <---
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

            // 3. Cuerpo del JSON
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "Eres un asistente educativo útil."),
                    Map.of("role", "user", "content", prompt)
            ));
            body.put("temperature", 0.5);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // 4. Llamada a la API
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "El tutor analizó la información pero no generó respuesta.";

        } catch (Exception e) {
            e.printStackTrace();
            // Mensaje amigable para el usuario final
            return "⚠️ Hubo un problema de conexión segura con el Tutor IA. Por favor intenta de nuevo en 10 segundos.";
        }
    }
}