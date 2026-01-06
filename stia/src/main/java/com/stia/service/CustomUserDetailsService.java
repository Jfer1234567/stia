package com.stia.service;

import com.stia.entity.Usuario;
import com.stia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Buscamos el usuario en TU base de datos Postgres
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. Lo convertimos a un objeto que Spring Security entienda
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword()) // Spring verificará el hash aquí
                .roles(usuario.getRol().name()) // Asigna el rol (Ej: "DOCENTE")
                .build();
    }
}