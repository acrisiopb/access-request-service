package com.acrisio.accesscontrol.api.controller;

import com.acrisio.accesscontrol.api.dto.AuthLoginRequest;
import com.acrisio.accesscontrol.api.dto.UserCreateDTO;
import com.acrisio.accesscontrol.api.dto.UserDTO;
import com.acrisio.accesscontrol.domain.enums.Department;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    String token;

    private static final Long EXISTING_USER_ID = 1L;
    private static final Long NON_EXISTENT_USER_ID = 999L;
    private static final String EXISTING_EMAIL = "alice@corp.com";


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

    // TESTES DE CRIAÇÃO (POST /user)

    @Test
    void createUser_Success() throws Exception {
        UserCreateDTO createDto = new UserCreateDTO("Novo User", "novo@corp.com", Department.OTHER, "senha123456");

        MvcResult res = mockMvc.perform(post("/user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("novo@corp.com"))
                .andReturn();

        // Limpeza (Deletando o usuário criado dinamicamente)
        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        Long newUserId = node.get("id").asLong();
        UserDTO deleteDto = new UserDTO(newUserId, null, null, null);

        mockMvc.perform(delete("/user")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteDto)));
    }

    @Test
    void createUser_EmailAlreadyExists_Returns409() throws Exception {
        UserCreateDTO createDto = new UserCreateDTO("Duplicate User", EXISTING_EMAIL, Department.OTHER, "senha123456");

        mockMvc.perform(post("/user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This email address has already been registered."));
    }

    // TESTES DE BUSCA

    @Test
    void findById_Success() throws Exception {
        UserDTO findDto = new UserDTO(EXISTING_USER_ID, null, null, null);

        mockMvc.perform(post("/user/find")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(findDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(EXISTING_USER_ID));
    }

    @Test
    void findById_NotFound_Returns404() throws Exception {
        UserDTO findDto = new UserDTO(NON_EXISTENT_USER_ID, null, null, null);

        mockMvc.perform(post("/user/find")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(findDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found."));
    }

    @Test
    void findAll_ReturnsAllUsers() throws Exception {
        mockMvc.perform(get("/user")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(EXISTING_USER_ID));
    }

    // TESTES DE DELEÇÃO

    @Test
    void deleteUser_NotFound_Returns404() throws Exception {
        UserDTO deleteDto = new UserDTO(NON_EXISTENT_USER_ID, null, null, null);

        mockMvc.perform(delete("/user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found."));
    }
}