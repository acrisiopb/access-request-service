package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuplicateActiveAccessRuleUnitTests {

    @Mock
    private InternationalizationUtil message;

    @InjectMocks
    private DuplicateActiveAccessRule rule;

    private User user;
    private Module requestedModule;
    private AccessRequestCreateDTO dto;
    private static final Long USER_ID = 1L;
    private static final Long MODULE_ID = 10L;
    private static final String MODULE_NAME = "AUDITORIA";
    private static final String MESSAGE_KEY = "rule.duplicateActiveAccessRule.info";

    @BeforeEach
    void setUp() {
        user = User.builder().id(USER_ID).accesses(new ArrayList<>()).build();
        requestedModule = Module.builder().id(MODULE_ID).name(MODULE_NAME).build();
        dto = new AccessRequestCreateDTO(USER_ID, List.of(MODULE_ID), "Valid justification.", true);

        lenient().when(message.getMessage(eq(MESSAGE_KEY))).thenReturn("The user already has active access to the module.");
    }

    @Test
    void validate_NoActiveAccess_Success() {
        // user.accesses está vazio
        assertDoesNotThrow(() -> rule.validate(user, Set.of(requestedModule), dto));
    }

    @Test
    void validate_ExpiredAccessForSameModule_Success() {

        Module expiredModule = Module.builder().id(MODULE_ID).name(MODULE_NAME).build();
        Access expiredAccess = Access.builder()
                .module(expiredModule)
                .expiresAt(OffsetDateTime.now().minusDays(1))
                .build();

        // Adiciona o acesso expirado, que será filtrado por getActiveAccesses()
        user.getAccesses().add(expiredAccess);

        assertDoesNotThrow(() -> rule.validate(user, Set.of(requestedModule), dto));
    }

    @Test
    void validate_ActiveAccessForSameModule_ThrowsIllegalArgumentException() {
        Module activeModule = Module.builder().id(MODULE_ID).name(MODULE_NAME).build();
        Access activeAccess = Access.builder()
                .module(activeModule)
                .expiresAt(OffsetDateTime.now().plusDays(10))
                .build();

        user.getAccesses().add(activeAccess);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                rule.validate(user, Set.of(requestedModule), dto));

        String expectedMessage = "The user already has active access to the module. " + MODULE_NAME;
        assertEquals(expectedMessage, exception.getMessage());

        verify(message, times(1)).getMessage(eq(MESSAGE_KEY));
    }
}