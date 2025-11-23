package com.acrisio.accesscontrol.domain.rules;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import com.acrisio.accesscontrol.domain.model.AccessRequest;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.domain.repository.AccessRequestRepository;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuplicateActiveRequestRuleUnitTests {

    @Mock
    private AccessRequestRepository accessRequestRepository;

    @Mock
    private InternationalizationUtil message;

    @InjectMocks
    private DuplicateActiveRequestRule rule;

    private User user;
    private Module requestedModule;
    private AccessRequestCreateDTO dto;
    private static final Long USER_ID = 1L;
    private static final Long MODULE_ID = 10L;
    private static final String MODULE_NAME = "AUDITORIA";
    private static final String MESSAGE_KEY = "rule.duplicateActiveRequestRule.info";

    @BeforeEach
    void setUp() {
        user = User.builder().id(USER_ID).email("test@user.com").build();
        requestedModule = Module.builder().id(MODULE_ID).name(MODULE_NAME).build();
        dto = new AccessRequestCreateDTO(USER_ID, List.of(MODULE_ID), "Valid justification.", true);
        lenient().when(message.getMessage(eq(MESSAGE_KEY))).thenReturn("User already has an active request for the module:");
    }

    @Test
    void validate_NoActiveRequest_Success() {
        when(accessRequestRepository.findByUser(eq(user))).thenReturn(List.of());
        assertDoesNotThrow(() -> rule.validate(user, Set.of(requestedModule), dto));
        verify(accessRequestRepository, times(1)).findByUser(eq(user));
    }

    @Test
    void validate_ExistingActiveRequestForSameModule_ThrowsIllegalArgumentException() {
        AccessRequest activeReq = AccessRequest.builder()
                .status(RequestStatus.ACTIVE)
                .modules(Set.of(requestedModule))
                .build();

        when(accessRequestRepository.findByUser(eq(user))).thenReturn(List.of(activeReq));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                rule.validate(user, Set.of(requestedModule), dto));

        String expectedMessage = "User already has an active request for the module: " + MODULE_NAME;
        assertEquals(expectedMessage, exception.getMessage());

        verify(accessRequestRepository, times(1)).findByUser(eq(user));
        verify(message, times(1)).getMessage(eq(MESSAGE_KEY));
    }
}