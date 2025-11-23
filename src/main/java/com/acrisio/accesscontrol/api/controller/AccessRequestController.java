package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.*;
import com.acrisio.accesscontrol.exception.ErrorMessage;
import com.acrisio.accesscontrol.infrastructure.security.CurrentUserProvider;
import com.acrisio.accesscontrol.service.AccessRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/request")
@RequiredArgsConstructor
@Tag(name = "Solicitação de Acesso", description = "Contém todas as operações relativas aos recursos para as solicitações de acesso.")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = AccessRequestResponseDTO.class)) }),
        @ApiResponse(responseCode = "404", description = "user not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) })
})
public class AccessRequestController {

    private final AccessRequestService accessRequestService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @Operation(summary = "Solicitação de acesso", description = "Usuário solicita acesso ao módulo desejado.")
    public ResponseEntity<AccessRequestResponseDTO> create(
            @Valid @RequestBody AccessRequestCreateInput input) {

        var user = currentUserProvider.get();
        var dto = new AccessRequestCreateDTO(user.getId(), input.moduleIds(), input.justification(), input.urgent());
        return ResponseEntity.ok(accessRequestService.createRequest(dto));
    }

    @Operation(summary = "Cancelar solicitação de acesso", description = "Cancelamento da solicitação por ID da solicitação e motivo. O usuário só pode cancelar suas solicitações.")
    @PostMapping("/cancel")
    public ResponseEntity<AccessRequestResponseDTO> cancel(@RequestBody AccessRequestCancelDTO dto) {

        var currentUser = currentUserProvider.get();

        return ResponseEntity.ok(
                accessRequestService.cancel(
                        dto.id(),
                        currentUser.getId(),
                        dto.reason()
                )
        );
    }


    @Operation(summary = "Renovar solicitação de acesso", description = "Renovação por ID da solicitação.")
    @PostMapping("/renew")
    public ResponseEntity<AccessRequestResponseDTO> renew(
            @RequestBody AccessRequestIdDTO dto) {

        return ResponseEntity.ok(accessRequestService.renew(dto.id()));
    }

    @Operation(summary = "Buscar todas as solicitações do usuário autenticado",
            description = "Retorna automaticamente todas as solicitações vinculadas ao usuário do token.")
    @GetMapping("/find")
    public ResponseEntity<List<AccessRequestResponseDTO>> find() {

        var currentUser = currentUserProvider.get();
        Long userIdFromToken = currentUser.getId();

        List<AccessRequestResponseDTO> list =
                accessRequestService.listByUser(userIdFromToken);

        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Excluir solicitação", description = "Exclusão por ID da solicitação.")
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody AccessRequestIdDTO dto) {
        accessRequestService.delete(dto.id());
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Filtrar solicitações de acesso",
            description = """
        Permite buscar solicitações de acesso utilizando múltiplos critérios de filtro.
        Todos os campos do objeto 'filter' são opcionais e podem ser combinados para refinar a busca.

        Parâmetros de filtro:
        • filter.search – Texto para busca geral (módulo, usuário, justificativa, etc.)
        • filter.status – Filtrar por status (ACTIVE, APPROVED, CANCELED, REJECTED...)
        • filter.urgent – Filtrar apenas solicitações urgentes
        • filter.startDate – Data inicial de criação (yyyy-MM-dd)
        • filter.endDate – Data final de criação (yyyy-MM-dd)

        Paginação (pageable):
        • pageable.page – Número da página (0 é a primeira)
        • pageable.size – Quantidade de itens por página
        • pageable.sort – Ordenação (ex.: 'createdAt,desc')

        Exemplo de requisição no Swagger:

        filter:
        {
          "search": "RH",
          "status": "ACTIVE",
          "urgent": true,
          "startDate": "2025-11-01",
          "endDate": "2025-11-22"
        }

        pageable:
        {
          "page": 0,
          "size": 10,
          "sort": ["createdAt,desc"]
        }
        """
    )

    @GetMapping("/filter")
    public Page<AccessRequestResponseDTO> filter(
            AccessRequestFilterDTO filter,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var user = currentUserProvider.get();
        return accessRequestService.filter(user.getId(), filter, pageable);
    }
}
