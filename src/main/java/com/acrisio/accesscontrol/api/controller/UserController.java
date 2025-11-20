package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.UserCreateDTO;
import com.acrisio.accesscontrol.api.dto.UserDTO;
import com.acrisio.accesscontrol.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@Tag(name = "Usuário", description = "Contém todas as operações relativas aos recursos para o gerenciamento de usuário.")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation =  ErrorMessage.class)) }),
        @ApiResponse(responseCode = "404", description = "user not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation =  ErrorMessage.class)) })
})
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Criar um novo usuário.", description = "Adicionar um novo usuário. Usuário com Departamentos [TI], podem ser adicionado até 10 Módulos" +
           "Caso não seja, apenas 1 a 3 módulos é aceito. ")
    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserCreateDTO dto) {
        return ResponseEntity.ok(userService.create(dto));
    }
    @Operation(summary = "Buscar todos os usuários.", description = "Buscar todos os usuários cadastrado.")
    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }
    @Operation(summary = "Buscar usuários por ID.", description = "Buscar usuário cadastrado por ID.")
    @PostMapping("/find")
    public ResponseEntity<UserDTO> find(@RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.findById(dto.id()));
    }

    @Operation(summary = "Atualizar usuário.", description = "Atualizar dados do usuário.")
    @PutMapping
    public ResponseEntity<UserDTO> update(@Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.update(dto));
    }

    @Operation(summary = "Excluir usuário.", description = "Exclusão de usuário por ID.")
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody UserDTO dto) {
        userService.delete(dto.id());
        return ResponseEntity.noContent().build();
    }
}
