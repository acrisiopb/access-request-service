package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.enums.Department;
import com.acrisio.accesscontrol.domain.model.Access;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessRulesUnitTests {

    @Mock
    private InternationalizationUtil message;

    // Injeta o mock 'message' nas regras de negócio
    @InjectMocks
    private DepartmentPermissionRule departmentPermissionRule;
    @InjectMocks
    private ModuleLimitRule moduleLimitRule;
    @InjectMocks
    private ModuleCompatibilityRule moduleCompatibilityRule;
    @InjectMocks
    private JustificationRule justificationRule;

    // Test data
    private AccessRequestCreateDTO validDto;
    private Module portalModule;
    private User tiUser;
    private User financeUser;
    private User rhUser;

    @BeforeEach
    void setUp() {
        lenient().when(message.getMessage(eq("rule.departmentPermissionRule.info"))).thenReturn("Department rule failed:");
        lenient().when(message.getMessage(eq("rule.moduleLimitRule.info"))).thenReturn("Limit reached for department:");
        lenient().when(message.getMessage(eq("rule.moduleLimitRule.infoII"))).thenReturn("Max allowed is:");
        lenient().when(message.getMessage(eq("rule.moduleCompatibilityRule.infoI"))).thenReturn("Requested incompatible:");
        lenient().when(message.getMessage(eq("rule.moduleCompatibilityRule.infoII"))).thenReturn("with active:");
        lenient().when(message.getMessage(eq("rule.justificationRule.info"))).thenReturn("Justification required.");
        lenient().when(message.getMessage(eq("rule.justificationRule.infoII"))).thenReturn("Length invalid.");
        lenient().when(message.getMessage(eq("rule.justificationRule.infoIII"))).thenReturn("Generic or repetitive content.");

        validDto = new AccessRequestCreateDTO(1L, List.of(1L), "Justificativa válida com mais de vinte caracteres e específica.", false);

        // Setup Modules
        portalModule = Module.builder().id(1L).name("PORTAL").build();

        // Setup Users (usando mock de lista vazia para 'accesses' para evitar NPE se a regra não verificar nulos)
        tiUser = User.builder().id(10L).department(Department.TI).accesses(new ArrayList<>()).build();
        financeUser = User.builder().id(20L).department(Department.FINANCE).accesses(new ArrayList<>()).build();
        rhUser = User.builder().id(30L).department(Department.RH).accesses(new ArrayList<>()).build();
    }

    //  DepartmentPermissionRule Tests

    @Test
    void departmentPermissionRule_TISuccess() {
        // TI pode acessar todos os módulos
        Module auditModule = Module.builder().id(10L).name("AUDITORIA").build();
        assertDoesNotThrow(() -> departmentPermissionRule.validate(tiUser, Set.of(auditModule), validDto));
    }

    @Test
    void departmentPermissionRule_FinanceDeniedForRHModule() {
        // Financeiro tentando acessar módulo exclusivo de RH
        Module adminRH = Module.builder().id(6L).name("ADMINISTRADOR_RH").build();
        assertThrows(IllegalArgumentException.class, () ->
                departmentPermissionRule.validate(financeUser, Set.of(adminRH), validDto));
    }

    @Test
    void departmentPermissionRule_RHDeniedForEstoqueModule() {
        // RH tentando acessar módulo exclusivo de Operações
        Module estoque = Module.builder().id(8L).name("ESTOQUE").build();
        assertThrows(IllegalArgumentException.class, () ->
                departmentPermissionRule.validate(rhUser, Set.of(estoque), validDto));
    }

    @Test
    void departmentPermissionRule_FinanceSuccessForFinanceModule() {
        // Financeiro solicitando módulo permitido (GESTAO_FINANCEIRA)
        Module finance = Module.builder().id(3L).name("GESTAO_FINANCEIRA").build();
        assertDoesNotThrow(() -> departmentPermissionRule.validate(financeUser, Set.of(finance), validDto));
    }

    // ModuleLimitRule Tests

    @Test
    void moduleLimitRule_NonTILimitExceeded_ThrowsException() {

        // Limite para não-TI é 5. Adiciona 5 acessos ativos. Tenta solicitar mais 1.
        List<Access> activeAccesses = IntStream.range(1, 6)
                .mapToObj(i -> Access.builder().expiresAt(OffsetDateTime.now().plusDays(180)).module(Module.builder().id((long) i).build()).build())
                .collect(Collectors.toList());
        financeUser.setAccesses(activeAccesses); // Set acessos para o usuário

        Module extraModule = Module.builder().id(99L).build();

        assertThrows(IllegalArgumentException.class, () ->
                moduleLimitRule.validate(financeUser, Set.of(extraModule), validDto));
    }

    @Test
    void moduleLimitRule_TILimitNotExceeded_Success() {
        // Limite para TI é 10. Adiciona 9 acessos ativos. Tenta solicitar mais 1.
        List<Access> activeAccesses = IntStream.range(1, 10)
                .mapToObj(i -> Access.builder().expiresAt(OffsetDateTime.now().plusDays(180)).module(Module.builder().id((long) i).build()).build())
                .collect(Collectors.toList());
        tiUser.setAccesses(activeAccesses); // Set acessos para o usuário

        Module extraModule = Module.builder().id(99L).build();

        assertDoesNotThrow(() -> moduleLimitRule.validate(tiUser, Set.of(extraModule), validDto));
    }


    // ModuleCompatibilityRule Tests

    @Test
    void moduleCompatibilityRule_IncompatibilityFound_ThrowsException() {
        // Módulos: Aprovador (4) e Solicitante (5) são incompatíveis (simulado).
        Module aprovador = Module.builder().id(4L).name("APROVADOR").build();
        Module solicitante = Module.builder().id(5L).name("SOLICITANTE").incompatibleModules(Set.of(aprovador)).build();

        // Spy no usuário para simular getActiveModules e setar os módulos ativos.
        User spyUser = spy(financeUser);
        when(spyUser.getActiveModules()).thenReturn(Set.of(aprovador)); // Mock retorna o módulo ativo

        // Tentar solicitar SOLICITANTE
        assertThrows(IllegalArgumentException.class, () ->
                moduleCompatibilityRule.validate(spyUser, Set.of(solicitante), validDto));
    }

    @Test
    void moduleCompatibilityRule_NoIncompatibility_Success() {
        // Módulos: Aprovador (4) e Relatórios (2) são compatíveis.
        Module aprovador = Module.builder().id(4L).name("APROVADOR").build();
        Module relatorios = Module.builder().id(2L).name("RELATORIOS").incompatibleModules(Collections.emptySet()).build();

        User spyUser = spy(financeUser);
        when(spyUser.getActiveModules()).thenReturn(Set.of(aprovador));

        // Tentar solicitar RELATORIOS
        assertDoesNotThrow(() -> moduleCompatibilityRule.validate(spyUser, Set.of(relatorios), validDto));
    }


    // JustificationRule Tests

    @Test
    void justificationRule_ShortJustification_ThrowsException() {
        // Justificativa muito curta (< 20 caracteres)
        AccessRequestCreateDTO dto = new AccessRequestCreateDTO(1L, List.of(1L), "teste", false);
        assertThrows(IllegalArgumentException.class, () ->
                justificationRule.validate(tiUser, Set.of(portalModule), dto));
    }

    @Test
    void justificationRule_GenericJustification_ThrowsException() {
        // Justificativa com palavra genérica da lista negra ("preciso")
        AccessRequestCreateDTO dto = new AccessRequestCreateDTO(1L, List.of(1L), "Preciso de acesso urgente para o novo projeto.", false);
        assertThrows(IllegalArgumentException.class, () ->
                justificationRule.validate(tiUser, Set.of(portalModule), dto));
    }

    @Test
    void justificationRule_RepetitiveCharacters_ThrowsException() {
        // Justificativa com repetição exagerada de caracteres ("aaaaa")
        AccessRequestCreateDTO dto = new AccessRequestCreateDTO(1L, List.of(1L), "aaaaa Teste de repetição de caracteres.", false);
        assertThrows(IllegalArgumentException.class, () ->
                justificationRule.validate(tiUser, Set.of(portalModule), dto));
    }

    @Test
    void justificationRule_ValidJustification_Success() {
        // Justificativa com comprimento e conteúdo válidos
        AccessRequestCreateDTO dto = new AccessRequestCreateDTO(1L, List.of(1L), "Solicitação de acesso necessária para o cumprimento integral da demanda operacional do mês de novembro.", false);
        assertDoesNotThrow(() -> justificationRule.validate(tiUser, Set.of(portalModule), dto));
    }
}