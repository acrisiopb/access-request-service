package com.acrisio.accesscontrol.service;

import com.acrisio.accesscontrol.api.dto.AccessResponseDTO;
import com.acrisio.accesscontrol.domain.enums.HistoryAction;
import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import com.acrisio.accesscontrol.domain.model.*;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.repository.AccessRepositoy;
import com.acrisio.accesscontrol.domain.repository.AccessRequestRepository;
import com.acrisio.accesscontrol.domain.repository.RequestHistoryRepository;
import com.acrisio.accesscontrol.domain.repository.UserRepository;
import com.acrisio.accesscontrol.domain.rules.AccessRequestRule;
import com.acrisio.accesscontrol.exception.EntityNotFoundException;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessServiceUnitTests {

    @Mock
    private AccessRepositoy accessRepository;
    @Mock
    private AccessRequestRepository accessRequestRepository;
    @Mock
    private RequestHistoryRepository requestHistoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InternationalizationUtil message;
    @Mock
    private AccessRequestService accessRequestService;

    @Mock
    private List<AccessRequestRule> rules;

    @InjectMocks
    private AccessService accessService;

    private User user;
    private Module module;
    private Access activeAccess;

    private static final Long ACCESS_ID = 50L;
    private static final Long USER_ID = 1L;
    private static final Long MODULE_ID = 10L;
    private static final String MODULE_NAME = "TEST_MOD";
    private static final String PROTOCOL = "SOL-20251122-0001";
    private static final OffsetDateTime GRANTED_AT = OffsetDateTime.now().minusDays(30);

    @BeforeEach
    void setUp() {
        user = User.builder().id(USER_ID).name("Test User").accesses(new ArrayList<>()).build();
        module = Module.builder().id(MODULE_ID).name(MODULE_NAME).active(true).build();
        activeAccess = Access.builder()
                .id(ACCESS_ID)
                .user(user)
                .module(module)
                .grantedAt(GRANTED_AT)
                .expiresAt(OffsetDateTime.now().plusDays(10))
                .build();

        lenient().when(message.getMessage(eq("Access.notfound"))).thenReturn("Access not found.");
        lenient().when(message.getMessage(eq("Access.renew.active"))).thenReturn("Only ACTIVE accesses can be renewed.");
        lenient().when(message.getMessage(eq("Access.info.renew"))).thenReturn("Renewal is only permitted when there are less than 30 days left until expiration.");
        lenient().when(message.getMessage(eq("Access.renew"))).thenReturn("Automatic module renewal.");
        lenient().when(message.getMessage(eq("User.notfound"))).thenReturn("User not found.");
    }

    @Test
    void revoke_Success() {
        when(accessRepository.findById(eq(ACCESS_ID))).thenReturn(Optional.of(activeAccess));
        doNothing().when(accessRepository).delete(eq(activeAccess));

        accessService.revoke(ACCESS_ID);

        verify(accessRepository, times(1)).delete(eq(activeAccess));
    }

    @Test
    void revoke_AccessNotFound_ThrowsEntityNotFoundException() {
        when(accessRepository.findById(eq(ACCESS_ID))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                accessService.revoke(ACCESS_ID));

        assertEquals("Access not found.", exception.getMessage());
        verify(accessRepository, only()).findById(eq(ACCESS_ID));
    }

    @Test
    void renew_Success_CreatesNewRequestAndExtendsAccess() {
        when(accessRepository.findById(eq(ACCESS_ID))).thenReturn(Optional.of(activeAccess));
        when(accessRequestService.generateProtocol()).thenReturn(PROTOCOL);
        when(rules.iterator()).thenReturn(java.util.Collections.emptyIterator());

        ArgumentCaptor<Access> accessCaptor = ArgumentCaptor.forClass(Access.class);
        when(accessRepository.save(accessCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<AccessRequest> requestCaptor = ArgumentCaptor.forClass(AccessRequest.class);
        when(accessRequestRepository.save(requestCaptor.capture())).thenAnswer(i -> {
            AccessRequest req = requestCaptor.getValue();
            req.setId(1L);
            return req;
        });

        AccessResponseDTO result = accessService.renew(ACCESS_ID, USER_ID);
        assertNotNull(result);

        Access savedAccess = accessCaptor.getValue();
        verify(accessRepository, times(1)).save(eq(savedAccess));
        assertTrue(savedAccess.getExpiresAt().isAfter(OffsetDateTime.now().plusDays(179)));

        AccessRequest savedRequest = requestCaptor.getValue();
        assertEquals(RequestStatus.ACTIVE, savedRequest.getStatus());
        assertEquals(PROTOCOL, savedRequest.getProtocol());
        assertTrue(savedRequest.getJustification().contains(MODULE_NAME));
        verify(accessRequestRepository, times(1)).save(eq(savedRequest));

        verify(requestHistoryRepository, times(1)).save(argThat(history ->
                history.getAction() == HistoryAction.RENEWED
        ));

        verify(rules, times(1)).iterator();
    }

    @Test
    void renew_RuleDenied_NewRequestIsDeniedAndAccessIsNotExtended() {
        when(accessRepository.findById(eq(ACCESS_ID))).thenReturn(Optional.of(activeAccess));
        when(accessRequestService.generateProtocol()).thenReturn(PROTOCOL);

        final String DENIAL_REASON = "Rule violation: incompatible.";
        AccessRequestRule denyingRule = (u, ms, d) -> { throw new IllegalArgumentException(DENIAL_REASON); };

        java.util.Iterator<AccessRequestRule> mockIterator = mock(java.util.Iterator.class);
        when(mockIterator.hasNext()).thenReturn(true, false);
        when(mockIterator.next()).thenReturn(denyingRule);
        when(rules.iterator()).thenReturn(mockIterator);

        ArgumentCaptor<AccessRequest> requestCaptor = ArgumentCaptor.forClass(AccessRequest.class);
        when(accessRequestRepository.save(requestCaptor.capture())).thenAnswer(i -> {
            AccessRequest req = requestCaptor.getValue();
            req.setId(1L);
            return req;
        });

        OffsetDateTime originalExpiration = activeAccess.getExpiresAt();

        accessService.renew(ACCESS_ID, USER_ID);

        AccessRequest savedRequest = requestCaptor.getValue();
        assertEquals(RequestStatus.DENIED, savedRequest.getStatus());
        assertEquals(DENIAL_REASON, savedRequest.getDeniedReason());
        verify(accessRequestRepository, times(1)).save(eq(savedRequest));

        verify(accessRepository, times(1)).findById(eq(ACCESS_ID));
        verifyNoMoreInteractions(accessRepository);

        verifyNoInteractions(requestHistoryRepository);

        verify(rules, times(1)).iterator();
    }

    @Test
    void renew_AccessExpired_ThrowsIllegalArgumentException() {
        Access expiredAccess = Access.builder()
                .id(ACCESS_ID)
                .user(user)
                .module(module)
                .expiresAt(OffsetDateTime.now().minusDays(1))
                .build();

        when(accessRepository.findById(eq(ACCESS_ID))).thenReturn(Optional.of(expiredAccess));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                accessService.renew(ACCESS_ID, USER_ID));

        assertEquals("Only ACTIVE accesses can be renewed.", exception.getMessage());
        verify(rules, never()).iterator();
    }

    @Test
    void renew_ExpiresInTooLong_ThrowsIllegalArgumentException() {
        Access tooLongAccess = Access.builder()
                .id(ACCESS_ID)
                .user(user)
                .module(module)
                .expiresAt(OffsetDateTime.now().plusDays(31))
                .build();

        when(accessRepository.findById(eq(ACCESS_ID))).thenReturn(Optional.of(tooLongAccess));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                accessService.renew(ACCESS_ID, USER_ID));

        assertEquals("Renewal is only permitted when there are less than 30 days left until expiration.", exception.getMessage());
        verify(rules, never()).iterator();
    }

    @Test
    void renew_NotUserOwner_ThrowsEntityNotFoundException() {
        Long otherUserId = 99L;
        when(accessRepository.findById(eq(ACCESS_ID))).thenReturn(Optional.of(activeAccess));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                accessService.renew(ACCESS_ID, otherUserId));

        assertEquals("Access not found.", exception.getMessage());
        verify(rules, never()).iterator();
    }

    @Test
    void findByUser_Success() {
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(user));
        when(accessRepository.findByUser(eq(user))).thenReturn(List.of(activeAccess));

        List<AccessResponseDTO> result = accessService.findByUser(USER_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        verify(userRepository, times(1)).findById(eq(USER_ID));
        verify(accessRepository, times(1)).findByUser(eq(user));
    }

    @Test
    void findById_Success() {
        when(accessRepository.findById(eq(ACCESS_ID))).thenReturn(Optional.of(activeAccess));

        AccessResponseDTO result = accessService.findById(ACCESS_ID);

        assertNotNull(result);

        verify(accessRepository, times(1)).findById(eq(ACCESS_ID));
    }

    @Test
    void findById_NotFound_ThrowsEntityNotFoundException() {
        when(accessRepository.findById(eq(ACCESS_ID))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> accessService.findById(ACCESS_ID));

        verify(message, times(1)).getMessage(eq("Access.notfound"));
    }

    @Test
    void findAll_ReturnsAllAccesses() {
        Access access2 = Access.builder().id(51L).user(user).module(module).grantedAt(GRANTED_AT).expiresAt(OffsetDateTime.now().plusDays(10)).build();
        when(accessRepository.findAll()).thenReturn(List.of(activeAccess, access2));

        List<AccessResponseDTO> result = accessService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(accessRepository, times(1)).findAll();
    }
}