package com.stia.controller;

import com.stia.service.StiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StiaController {

    @Autowired
    private StiaService stiaService;

    @PostMapping("/aprender")
    public ResponseEntity<?> subirPdf(@RequestParam("archivo") MultipartFile archivo) {
        try {
            String mensaje = stiaService.guardarConocimiento(archivo);
            return ResponseEntity.ok(Map.of("mensaje", mensaje));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/preguntar")
    public ResponseEntity<?> preguntar(@RequestBody Map<String, String> payload) {
        String pregunta = payload.get("pregunta");
        String respuesta = stiaService.consultarTutor(pregunta);
        return ResponseEntity.ok(Map.of("respuesta", respuesta));
    }
}