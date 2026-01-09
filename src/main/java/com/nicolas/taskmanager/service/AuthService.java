package com.nicolas.taskmanager.service;

import com.nicolas.taskmanager.dto.AuthResponse;
import com.nicolas.taskmanager.dto.LoginRequest;
import com.nicolas.taskmanager.dto.RegisterRequest;
import com.nicolas.taskmanager.model.User;
import com.nicolas.taskmanager.repository.UserRepository;
import com.nicolas.taskmanager.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Registro
    public AuthResponse register(RegisterRequest request) {
        // Verificar si el email ya existe
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Crear usuario
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Guardar en BD
        userRepository.save(user);

        // Generar token
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getEmail());
    }

    // Login
    public AuthResponse login(LoginRequest request) {
        // Buscar usuario
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // Generar token
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getEmail());
    }
}