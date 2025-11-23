package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.AccessIdDTO;
import com.acrisio.accesscontrol.api.dto.AccessResponseDTO;
import com.acrisio.accesscontrol.infrastructure.security.CurrentUserProvider;
import com.acrisio.accesscontrol.service.AccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.acrisio.accesscontrol.exception.ErrorMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/access")
@RequiredArgsConstructor
@Tag(name = "Acesso", description = "Contém todas as operações relativas aos recursos para o gerenciamento de acessos dos usuarios.")
@ApiResponses(value = {
        // CORREÇÃO AQUI: Use AccessResponseDTO.class
        @ApiResponse(responseCode = "200", description = "Successful operation", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = AccessResponseDTO.class)) }),

        @ApiResponse(responseCode = "404", description = "access not found"),
        // Lembre-se de corrigir o import do ErrorMessage aqui também para a sua classe personalizada
        @ApiResponse(responseCode = "500", description = "Internal server error", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) })
})
public class AccessController {

    private final AccessService accessService;
    private final CurrentUserProvider currentUserProvider;


    @Operation(summary = "Revogação de acesso de usuário ao módulo.", description = "Revogação de accesso de usuario ao módulo por ID do acesso.")
    @PostMapping("/revoke")
    public ResponseEntity<AccessResponseDTO> revoke(@RequestBody AccessIdDTO dto) {
        return ResponseEntity.ok(accessService.revoke(dto.id()));
    }
    @Operation(summary = "Renovar  accesso de usuário", description = " Renovar acesso do usuario por ID do acesso.")
    @PostMapping("/renew")
    public ResponseEntity<AccessResponseDTO> renew(@RequestBody AccessIdDTO dto) {

        var currentUser = currentUserProvider.get();

        return ResponseEntity.ok(
                accessService.renew(dto.id(), currentUser.getId())
        );
    }
    @Operation(summary = "Buscar de acesso.", description = "Buscar o acesso do usuário por ID do acesso.")
    @PostMapping("/find")
    public ResponseEntity<AccessResponseDTO> find(@RequestBody AccessIdDTO dto) {
        return ResponseEntity.ok(accessService.findById(dto.id()));
    }
    @Operation(summary = "Buscar pelo usuário.", description = "Buscar o usuário e seu acesso ao módulo por ID do acesso.")
    @PostMapping("/user")
    public ResponseEntity<List<AccessResponseDTO>> byUser(@RequestBody AccessIdDTO dto) {
        return ResponseEntity.ok(accessService.findByUser(dto.id()));
    }
    @Operation(summary = "Buscar todos usuários.", description = "Buscar todos usuários e seus acessos aos módulos.")
    @GetMapping
    public ResponseEntity<List<AccessResponseDTO>> findAll() {
        return ResponseEntity.ok(accessService.findAll());
    }
}
