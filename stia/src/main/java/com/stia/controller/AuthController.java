package com.stia.controller;

import com.stia.entity.Rol;
import com.stia.entity.Usuario;
import com.stia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- PANTALLA DE LOGIN ---
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // --- PANTALLA DE REGISTRO ---
    @GetMapping("/registro")
    public String registro(Model model) {
        // Enviamos la lista de alumnos por si se registra un Padre
        model.addAttribute("listaEstudiantes", usuarioRepository.findByRol(Rol.ESTUDIANTE));
        return "registro";
    }

    // --- PROCESAR REGISTRO (SEGURO) ---
    @PostMapping("/registro")
    public String registrarUsuario(@RequestParam String username,
                                   @RequestParam String password,
                                   @RequestParam String email,
                                   @RequestParam Rol rol,
                                   @RequestParam(required = false) Long hijoId, // Opcional (solo para Padres)
                                   Model model) {

        // 1. Validar si el usuario ya existe
        if (usuarioRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "El nombre de usuario ya está en uso.");
            model.addAttribute("listaEstudiantes", usuarioRepository.findByRol(Rol.ESTUDIANTE));
            return "registro";
        }

        // 2. Crear el Usuario Seguro
        Usuario nuevo = new Usuario();
        nuevo.setUsername(username);

        // --- AQUÍ ESTÁ LA MAGIA DE LA SEGURIDAD ---
        // Encriptamos la contraseña (ej: "1234" se convierte en "$2a$10$DkF...")
        nuevo.setPassword(passwordEncoder.encode(password));
        // -------------------------------------------

        nuevo.setEmail(email);
        nuevo.setRol(rol);

        usuarioRepository.save(nuevo);

        // 3. Lógica de Vinculación (Solo si es Padre y eligió un hijo)
        if (rol == Rol.PADRE && hijoId != null) {
            Usuario hijo = usuarioRepository.findById(hijoId).orElse(null);
            if (hijo != null) {
                // Guardamos el correo del padre en el perfil del hijo para las notificaciones
                hijo.setEmailPadre(email);
                usuarioRepository.save(hijo);
            }
        }

        return "redirect:/login?registrado=true";
    }
}