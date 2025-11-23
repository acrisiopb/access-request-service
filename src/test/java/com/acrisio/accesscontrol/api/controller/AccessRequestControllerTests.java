package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.AccessRequestCancelDTO;
import com.acrisio.accesscontrol.api.dto.AccessRequestCreateInput;
import com.acrisio.accesscontrol.api.dto.AccessRequestIdDTO;
import com.acrisio.accesscontrol.api.dto.AuthLoginRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AccessRequestControllerTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    String token;

    private static final Long MODULE_ID = 10L;

    @BeforeEach
    void auth() throws Exception {
        var body = new AuthLoginRequest("alice@corp.com", "alice123");
        var res = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();
        var json = res.getResponse().getContentAsString();
        var node = objectMapper.readTree(json);
        token = node.get("token").asText();
    }

    //Cria uma solicitação de acesso ativa e retorna seu ID.

    private Long createActiveRequestAndGetId() throws Exception {
        var dto = new AccessRequestCreateInput(List.of(MODULE_ID), "Justificativa detalhada e válida para criação de acesso que deve ser aprovado.", true);
        MvcResult res = mockMvc.perform(post("/request")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    //Cria uma solicitação reprovada e retorna seu ID.

    private Long createDeniedRequestAndGetId() throws Exception {

        var dto = new AccessRequestCreateInput(List.of(MODULE_ID), "Eu preciso de acesso imediato, pois a minha justificativa tem mais de 20 caracteres.", false);
        MvcResult res = mockMvc.perform(post("/request")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DENIED"))
                .andReturn();

        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        return node.get("id").asLong();
    }


    // TESTES DE CRIAÇÃO POST /request

    @Test
    void criarSolicitacaoSemTokenRetorna401() throws Exception {
        var dto = new AccessRequestCreateInput(List.of(MODULE_ID), "Solicitação com justificativa válida para teste e mais de 20 caracteres.", true);
        mockMvc.perform(post("/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criarSolicitacaoComTokenRetorna200() throws Exception {
        var dto = new AccessRequestCreateInput(List.of(MODULE_ID), "Solicitação com justificativa válida para teste e mais de 20 caracteres.", true);
        mockMvc.perform(post("/request")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocol").exists());
    }

    @Test
    void criarSolicitacaoComValidacaoFalhaRetorna422() throws Exception {
        var dto = new AccessRequestCreateInput(List.of(MODULE_ID), "curto", true);
        mockMvc.perform(post("/request")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Campo(s) inválido(s)"));
    }

    //TESTES DE CANCELAMENTO (POST /request/cancel)

    @Test
    void cancelarSolicitacao_Sucesso_Retorna200() throws Exception {
        Long requestId = createActiveRequestAndGetId();
        var cancelDto = new AccessRequestCancelDTO(requestId, "Motivo válido para o cancelamento da solicitação de acesso.");

        mockMvc.perform(post("/request/cancel")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        // Limpeza
        var deleteDto = new AccessRequestIdDTO(requestId);
        mockMvc.perform(delete("/request")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteDto)));
    }

    @Test
    void cancelarSolicitacao_MotivoCurto_Retorna401() throws Exception {
        Long requestId = createActiveRequestAndGetId();
        var cancelDto = new AccessRequestCancelDTO(requestId, "curto");

        mockMvc.perform(post("/request/cancel")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());

        // Limpeza
        var deleteDto = new AccessRequestIdDTO(requestId);
        mockMvc.perform(delete("/request")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteDto)));
    }

    //TESTES DE RENOVAÇÃO (POST /request/renew)

    @Test
    void renovarSolicitacao_StatusDenied_Retorna401() throws Exception {
        Long deniedRequestId = createDeniedRequestAndGetId();
        var renewDto = new AccessRequestIdDTO(deniedRequestId);

        mockMvc.perform(post("/request/renew")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renewDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("AccessRequest.cancelled"));

        // Limpeza
        var deleteDto = new AccessRequestIdDTO(deniedRequestId);
        mockMvc.perform(delete("/request")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteDto)));
    }


    //TESTES DE BUSCA/LISTAGEM (GET /request/find e /request/filter)

    @Test
    void buscarTodasAsSolicitacoesDoUsuario_Retorna200ELista() throws Exception {
        Long requestId = createActiveRequestAndGetId();

        mockMvc.perform(get("/request/find")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].protocol").exists());

        // Limpeza
        var deleteDto = new AccessRequestIdDTO(requestId);
        mockMvc.perform(delete("/request")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteDto)));
    }

    @Test
    void filtrarSolicitacoes_Retorna200EPageable() throws Exception {
        Long requestId = createActiveRequestAndGetId();
        var cancelDto = new AccessRequestCancelDTO(requestId, "Motivo válido para cancelamento que ultrapassa 10 caracteres.");

        mockMvc.perform(post("/request/cancel")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/request/filter?status=CANCELED&size=1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("CANCELED"));
    }


    //TESTES DE DELEÇÃO (DELETE /request)

    @Test
    void deletarSolicitacao_Sucesso_Retorna204() throws Exception {
        Long requestId = createActiveRequestAndGetId();
        var deleteDto = new AccessRequestIdDTO(requestId);

        mockMvc.perform(delete("/request")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isNoContent());

        // Tenta deletar novamente -> 404
        mockMvc.perform(delete("/request")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isNotFound());
    }
}