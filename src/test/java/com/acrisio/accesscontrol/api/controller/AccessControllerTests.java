package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.AccessIdDTO;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AccessControllerTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    String token;

    private Long existingAccessId;


    @BeforeEach
    void auth() throws Exception {
        var body = new AuthLoginRequest("alice@corp.com", "alice123");
        MvcResult res = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        token = node.get("token").asText();
    }

    //TESTES DE BUSCA

    @Test
    void findAll_ReturnsAllAccesses() throws Exception {
        mockMvc.perform(get("/access")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void findById_Success() throws Exception {
        MvcResult listRes = mockMvc.perform(get("/access")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode arr = objectMapper.readTree(listRes.getResponse().getContentAsString());
        existingAccessId = arr.get(0).get("id").asLong();
        var dto = new AccessIdDTO(existingAccessId);
        mockMvc.perform(post("/access/find")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingAccessId));
    }

    //TESTES DE RENOVAÇÃO

    @Test
    void renew_Success_ExtendsAccessCloseToExpiration() throws Exception {
        MvcResult listRes = mockMvc.perform(get("/access")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode arr = objectMapper.readTree(listRes.getResponse().getContentAsString());
        Long idToRenew = null;
        for (int i = 0; i < arr.size(); i++) {
            JsonNode item = arr.get(i);
            if (item.get("moduleName").asText().equals("GESTAO_FINANCEIRA") && item.get("userId").asLong() == 1L) {
                idToRenew = item.get("id").asLong();
                break;
            }
        }
        var renewDto = new AccessIdDTO(idToRenew);

        mockMvc.perform(post("/access/renew")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renewDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idToRenew))
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    void renew_AccessOwnedByAnotherUser_Returns404() throws Exception {
        MvcResult listRes = mockMvc.perform(get("/access")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode arr = objectMapper.readTree(listRes.getResponse().getContentAsString());
        Long otherUserAccessId = null;
        for (int i = 0; i < arr.size(); i++) {
            JsonNode item = arr.get(i);
            if (item.get("userId").asLong() != 1L) {
                otherUserAccessId = item.get("id").asLong();
                break;
            }
        }
        var renewDto = new AccessIdDTO(otherUserAccessId);

        mockMvc.perform(post("/access/renew")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renewDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Access.notfound"));
    }


    //TESTES DE REVOGAÇÃO

    @Test
    void revoke_Success() throws Exception {
        MvcResult listRes = mockMvc.perform(get("/access")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode arr = objectMapper.readTree(listRes.getResponse().getContentAsString());
        Long idToRevoke = arr.get(0).get("id").asLong();
        var revokeDto = new AccessIdDTO(idToRevoke);

        mockMvc.perform(post("/access/revoke")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revokeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idToRevoke));

        // Verifica que o acesso foi de fato removido
        mockMvc.perform(post("/access/find")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revokeDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByUser_Success() throws Exception {
        var dto = new AccessIdDTO(1L);
        mockMvc.perform(post("/access/user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(1L));
    }
}