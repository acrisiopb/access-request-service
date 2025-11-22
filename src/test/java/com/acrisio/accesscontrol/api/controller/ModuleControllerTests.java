package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.AuthLoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ModuleControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String token;

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

    @Test
    void listaModulosProtegidaRequerToken() throws Exception {
        mockMvc.perform(get("/modules"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listaModulosComTokenRetornaOk() throws Exception {
        mockMvc.perform(get("/modules").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }
}