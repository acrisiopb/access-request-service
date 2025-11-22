package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.AuthLoginRequest;
import com.acrisio.accesscontrol.api.dto.AuthResponseDTO;
import com.acrisio.accesscontrol.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login e emissão de token JWT.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica via e-mail e senha e retorna JWT.")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}