package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.api.dto.AccessRequestIdDTO;
import com.acrisio.accesscontrol.api.dto.AccessRequestResponseDTO;
import com.acrisio.accesscontrol.service.AccessRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.acrisio.accesscontrol.exception.ErrorMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/request")
@RequiredArgsConstructor
@Tag(name = "Solicitação de Acesso", description = "Contém todas as operações relativas aos recursos para as solicitações de acesso.")
@ApiResponses(value = {
        // CORREÇÃO AQUI: Use AccessRequestResponseDTO.class
        @ApiResponse(responseCode = "200", description = "Successful operation", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = AccessRequestResponseDTO.class)) }),

        @ApiResponse(responseCode = "404", description = "user not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) })
})
public class AccessRequestController {

    private final AccessRequestService accessRequestService;

    @PostMapping
    @Operation(summary = "Solicitação de accesso", description = "Usuário solicita acesso ao módulo desejado.")
    public ResponseEntity<AccessRequestResponseDTO> create(
            @Valid @RequestBody AccessRequestCreateDTO dto) {

        return ResponseEntity.ok(accessRequestService.createRequest( dto)
        );
    }
    @Operation(summary = "Cancelar Solicitação de accesso de usuário", description = "Cancelamento da solicitação de acesso do usuario por ID da solicitação.")
    @PostMapping("/cancel")
    public ResponseEntity<AccessRequestResponseDTO> cancel(
            @RequestBody AccessRequestIdDTO dto) {

        return ResponseEntity.ok(
                accessRequestService.cancel(dto.id())
        );
    }
    @Operation(summary = "Renovar Solicitação de accesso de usuário", description = " Renovar solicitação de acesso do usuario por ID da solicitação.")
    @PostMapping("/renew")
    public ResponseEntity<AccessRequestResponseDTO> renew(
            @RequestBody AccessRequestIdDTO dto) {

        return ResponseEntity.ok(
                accessRequestService.renew(dto.id())
        );
    }
    @Operation(summary = "Buscar Solicitação de accesso de usuário", description = "Encontrar solicitação de acesso do usuario por ID de solicitação.")
    @PostMapping("/find")
    public ResponseEntity<AccessRequestResponseDTO> find(
            @RequestBody AccessRequestIdDTO dto) {

        return ResponseEntity.ok(
                accessRequestService.findById(dto.id())
        );
    }
    @Operation(summary = "Excluir Solicitação de accesso", description = "Exclusão de solicitação de acesso por ID de solicitação.")
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody AccessRequestIdDTO dto) {

        accessRequestService.delete(dto.id());
        return ResponseEntity.noContent().build();
    }
}
