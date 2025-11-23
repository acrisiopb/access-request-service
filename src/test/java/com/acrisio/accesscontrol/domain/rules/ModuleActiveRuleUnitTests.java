package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ModuleActiveRuleUnitTests {

    @Mock
    private InternationalizationUtil message;

    @InjectMocks
    private ModuleActiveRule rule;

    private User user;
    private AccessRequestCreateDTO validDto;
    private static final String MESSAGE_KEY = "rule.moduleActiveRule.info";

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        validDto = new AccessRequestCreateDTO(1L, List.of(1L), "Justificativa longa e válida para teste de regra de módulo.", true);

        lenient().when(message.getMessage(eq(MESSAGE_KEY))).thenReturn("O módulo está inativo e não pode ser solicitado:");
    }

    @Test
    void validate_ModuleIsActive_Success() {
        Module activeModule = Module.builder().id(1L).name("ActiveModule").active(true).build();
        assertDoesNotThrow(() -> rule.validate(user, Set.of(activeModule), validDto));
    }

    @Test
    void validate_ModuleIsInactive_ThrowsIllegalArgumentException() {
        String moduleName = "InactiveModule";
        Module inactiveModule = Module.builder().id(2L).name(moduleName).active(false).build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                rule.validate(user, Set.of(inactiveModule), validDto));

        String expectedMessage = "O módulo está inativo e não pode ser solicitado: " + moduleName;
        assertEquals(expectedMessage, exception.getMessage());

        // Verifica a chamada específica da mensagem
        verify(message, times(1)).getMessage(eq(MESSAGE_KEY));
    }
}