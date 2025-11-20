package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.ModuleDTO;
import com.acrisio.accesscontrol.service.ModuleService;
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
@RequestMapping("/modules")
@RequiredArgsConstructor
@Tag(name = "Módulos", description = "Contém todas as operações relativas aos recursos para o gerenciamento dos módulos.")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation =  ErrorMessage.class)) }),
        @ApiResponse(responseCode = "404", description = "modules not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation =  ErrorMessage.class)) })
})
public class ModuleController {

    private final ModuleService moduleService;

    @Operation(summary = "Criar módulo.", description = "Criação de novo módulo")
    @PostMapping
    public ResponseEntity<ModuleDTO> create(@Valid @RequestBody ModuleDTO dto) {
        return ResponseEntity.ok(moduleService.create(dto));
    }

    @Operation(summary = "Buscar todos os módulos.", description = "Buscar todos os módulos cadastrados.")
    @GetMapping
    public ResponseEntity<List<ModuleDTO>> findAll() {
        return ResponseEntity.ok(moduleService.findAll());
    }

    @Operation(summary = "Buscar módulo por ID.", description = "Buscar módulo especifico por ID do módulo.")
    @PostMapping("/find")
    public ResponseEntity<ModuleDTO> find(@RequestBody ModuleDTO dto) {
        return ResponseEntity.ok(moduleService.findById(dto.id()));
    }

    @Operation(summary = "Atualizar módulo por ID.", description = "Atualizar os dados do módulo por ID do módulo.")
    @PutMapping
    public ResponseEntity<ModuleDTO> update(@Valid @RequestBody ModuleDTO dto) {
        return ResponseEntity.ok(moduleService.update(dto));
    }

    @Operation(summary = "Excluir módulo.", description = "Exclusão de módulo por ID do módulo.")
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody ModuleDTO dto) {
        moduleService.delete(dto.id());
        return ResponseEntity.noContent().build();
    }

}
