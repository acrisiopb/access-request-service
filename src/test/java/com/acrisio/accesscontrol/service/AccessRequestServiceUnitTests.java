package com.acrisio.accesscontrol.service;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.api.dto.AccessRequestFilterDTO;
import com.acrisio.accesscontrol.api.dto.AccessRequestResponseDTO;
import com.acrisio.accesscontrol.api.dto.ModuleDTO;
import com.acrisio.accesscontrol.domain.enums.HistoryAction;
import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import com.acrisio.accesscontrol.domain.model.*;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.repository.*;
import com.acrisio.accesscontrol.domain.rules.AccessRequestRule;
import com.acrisio.accesscontrol.exception.EntityNotFoundException;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessRequestServiceUnitTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ModuleRepository moduleRepository;
    @Mock
    private ProtocolSequenceRepository protocolSequenceRepository;
    @Mock
    private AccessRepositoy accessRepository;
    @Mock
    private AccessRequestRepository accessRequestRepository;
    @Mock
    private InternationalizationUtil message;
    @Mock
    private RequestHistoryRepository requestHistoryRepository;
    @Mock
    private List<AccessRequestRule> rules;

    @InjectMocks
    private AccessRequestService service;

    private User user;
    private Module module;

    private static final Long USER_ID = 1L;
    private static final Long MODULE_ID = 10L;

    @BeforeEach
    void init() {
        user = User.builder().id(USER_ID).name("User").accesses(new ArrayList<>()).build();
        module = Module.builder().id(MODULE_ID).name("MOD").active(true).permittedDepartments(new HashSet<>()).incompatibleModules(new HashSet<>()).build();
        lenient().when(message.getMessage(eq("User.notfound"))).thenReturn("User not found.");
        lenient().when(message.getMessage(eq("Module.notfound"))).thenReturn("Module not found.");
        lenient().when(message.getMessage(eq("AccessRequest.notfound"))).thenReturn("Request not found.");
        lenient().when(message.getMessage(eq("AccessRequest.active.cancelled"))).thenReturn("Only ACTIVE can be canceled.");
        lenient().when(message.getMessage(eq("AccessRequst.reason"))).thenReturn("Invalid reason.");
        lenient().when(message.getMessage(eq("AccessRequest.cancelled"))).thenReturn("Request cancelled.");
        lenient().when(message.getMessage(eq("AccessRequest.renew"))).thenReturn("Renewal");
        lenient().when(message.getMessage(eq("AccessRequest.info.renew"))).thenReturn("Info renew.");
        lenient().when(message.getMessage(eq("AccessRequest.renew.cancelled"))).thenReturn("Renew only for ACTIVE.");
    }

    @Test
    void createRequest_Success() {
        AccessRequestCreateDTO dto = new AccessRequestCreateDTO(USER_ID, List.of(MODULE_ID), "Justification", true);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(user));
        when(moduleRepository.findById(eq(MODULE_ID))).thenReturn(Optional.of(module));
        when(rules.iterator()).thenReturn(java.util.Collections.emptyIterator());

        ArgumentCaptor<AccessRequest> reqCaptor = ArgumentCaptor.forClass(AccessRequest.class);
        when(accessRequestRepository.save(reqCaptor.capture())).thenAnswer(i -> { AccessRequest r = reqCaptor.getValue(); r.setId(1L); return r; });

        ArgumentCaptor<Access> accessCaptor = ArgumentCaptor.forClass(Access.class);
        when(accessRepository.save(accessCaptor.capture())).thenAnswer(i -> accessCaptor.getValue());

        AccessRequestResponseDTO res = service.createRequest(dto);
        assertNotNull(res);

        AccessRequest saved = reqCaptor.getValue();
        assertEquals(RequestStatus.ACTIVE, saved.getStatus());
        assertEquals(dto.justification(), saved.getJustification());
        assertTrue(saved.getUrgent());
        verify(accessRequestRepository, times(1)).save(eq(saved));
        Access savedAccess = accessCaptor.getValue();
        assertEquals(user, savedAccess.getUser());
        assertEquals(module, savedAccess.getModule());
        verify(accessRepository, atLeastOnce()).save(eq(savedAccess));
    }

    @Test
    void createRequest_Denied() {
        AccessRequestCreateDTO dto = new AccessRequestCreateDTO(USER_ID, List.of(MODULE_ID), "Justification", false);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(user));
        when(moduleRepository.findById(eq(MODULE_ID))).thenReturn(Optional.of(module));
        AccessRequestRule denyingRule = (u, ms, d) -> { throw new IllegalArgumentException("Denied"); };
        java.util.Iterator<AccessRequestRule> it = mock(java.util.Iterator.class);
        when(it.hasNext()).thenReturn(true, false);
        when(it.next()).thenReturn(denyingRule);
        when(rules.iterator()).thenReturn(it);

        ArgumentCaptor<AccessRequest> reqCaptor = ArgumentCaptor.forClass(AccessRequest.class);
        when(accessRequestRepository.save(reqCaptor.capture())).thenAnswer(i -> reqCaptor.getValue());

        AccessRequestResponseDTO res = service.createRequest(dto);
        assertNotNull(res);
        AccessRequest saved = reqCaptor.getValue();
        assertEquals(RequestStatus.DENIED, saved.getStatus());
        assertEquals("Denied", saved.getDeniedReason());
        verify(accessRequestRepository, times(1)).save(eq(saved));
        verifyNoInteractions(accessRepository);
    }

    @Test
    void cancel_Success() {
        AccessRequest req = new AccessRequest();
        req.setId(1L);
        req.setUser(user);
        req.setModules(Set.of(module));
        req.setStatus(RequestStatus.ACTIVE);
        when(accessRequestRepository.findById(eq(1L))).thenReturn(Optional.of(req));

        Access access = Access.builder().id(100L).user(user).module(module).grantedAt(OffsetDateTime.now()).expiresAt(OffsetDateTime.now().plusDays(10)).build();
        user.setAccesses(List.of(access));

        ArgumentCaptor<RequestHistory> historyCaptor = ArgumentCaptor.forClass(RequestHistory.class);
        when(requestHistoryRepository.save(historyCaptor.capture())).thenAnswer(i -> historyCaptor.getValue());

        ArgumentCaptor<AccessRequest> reqCaptor = ArgumentCaptor.forClass(AccessRequest.class);
        when(accessRequestRepository.save(reqCaptor.capture())).thenAnswer(i -> reqCaptor.getValue());

        AccessRequestResponseDTO res = service.cancel(1L, USER_ID, "Valid reason for cancel");
        assertNotNull(res);
        verify(accessRepository, times(1)).delete(eq(access));
        RequestHistory h = historyCaptor.getValue();
        assertEquals(HistoryAction.CANCELED, h.getAction());
        verify(requestHistoryRepository, times(1)).save(eq(h));
        AccessRequest saved = reqCaptor.getValue();
        assertEquals(RequestStatus.CANCELED, saved.getStatus());
        verify(accessRequestRepository, times(1)).save(eq(saved));
    }

    @Test
    void cancel_NotOwner_Throws() {
        AccessRequest req = new AccessRequest();
        req.setId(1L);
        req.setUser(user);
        req.setModules(Set.of(module));
        req.setStatus(RequestStatus.ACTIVE);
        when(accessRequestRepository.findById(eq(1L))).thenReturn(Optional.of(req));
        assertThrows(EntityNotFoundException.class, () -> service.cancel(1L, 99L, "Valid reason for cancel"));
        verify(accessRequestRepository, times(1)).findById(eq(1L));
        verifyNoMoreInteractions(accessRequestRepository);
    }

    @Test
    void cancel_NotActive_Throws() {
        AccessRequest req = new AccessRequest();
        req.setId(1L);
        req.setUser(user);
        req.setModules(Set.of(module));
        req.setStatus(RequestStatus.DENIED);
        when(accessRequestRepository.findById(eq(1L))).thenReturn(Optional.of(req));
        assertThrows(IllegalArgumentException.class, () -> service.cancel(1L, USER_ID, "Valid reason for cancel"));
    }

    @Test
    void cancel_InvalidReason_Throws() {
        AccessRequest req = new AccessRequest();
        req.setId(1L);
        req.setUser(user);
        req.setModules(Set.of(module));
        req.setStatus(RequestStatus.ACTIVE);
        when(accessRequestRepository.findById(eq(1L))).thenReturn(Optional.of(req));
        assertThrows(IllegalArgumentException.class, () -> service.cancel(1L, USER_ID, "short"));
    }

    @Test
    void findById_Success() {
        AccessRequest req = new AccessRequest();
        req.setId(1L);
        req.setUser(user);
        when(accessRequestRepository.findById(eq(1L))).thenReturn(Optional.of(req));
        AccessRequestResponseDTO res = service.findById(1L, USER_ID);
        assertNotNull(res);
        verify(accessRequestRepository, times(1)).findById(eq(1L));
    }

    @Test
    void findById_NotOwner_Throws() {
        AccessRequest req = new AccessRequest();
        req.setId(1L);
        req.setUser(user);
        when(accessRequestRepository.findById(eq(1L))).thenReturn(Optional.of(req));
        assertThrows(EntityNotFoundException.class, () -> service.findById(1L, 99L));
    }

    @Test
    void listByUser_Success() {
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(user));
        AccessRequest r = new AccessRequest();
        r.setUser(user);
        when(accessRequestRepository.findByUser(eq(user))).thenReturn(List.of(r));
        List<AccessRequestResponseDTO> res = service.listByUser(USER_ID);
        assertFalse(res.isEmpty());
        verify(accessRequestRepository, times(1)).findByUser(eq(user));
    }

    @Test
    void listByUser_UserNotFound_Throws() {
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.listByUser(USER_ID));
    }

    @Test
    void delete_Success() {
        AccessRequest req = new AccessRequest();
        req.setId(1L);
        when(accessRequestRepository.findById(eq(1L))).thenReturn(Optional.of(req));
        service.delete(1L);
        verify(accessRequestRepository, times(1)).delete(eq(req));
    }

    @Test
    void delete_NotFound_Throws() {
        when(accessRequestRepository.findById(eq(1L))).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void generateProtocol_Increments() {
        String today = OffsetDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        ProtocolSequence seq = new ProtocolSequence(today, 0);
        when(protocolSequenceRepository.findById(eq(today))).thenReturn(Optional.of(seq));
        ArgumentCaptor<ProtocolSequence> cap = ArgumentCaptor.forClass(ProtocolSequence.class);
        when(protocolSequenceRepository.save(cap.capture())).thenReturn(seq);
        String protocol = service.generateProtocol();
        assertTrue(protocol.startsWith("SOL-" + today + "-"));
        ProtocolSequence saved = cap.getValue();
        assertEquals(1, saved.getCounter());
        verify(protocolSequenceRepository, times(1)).save(eq(saved));
    }

    @Test
    void filter_CallsRepositoryWithSpecAndPageable() {
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(user));
        AccessRequestFilterDTO filter = new AccessRequestFilterDTO(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        ArgumentCaptor<Specification<AccessRequest>> specCap = ArgumentCaptor.forClass(Specification.class);
        when(accessRequestRepository.findAll(specCap.capture(), eq(pageable))).thenReturn(new PageImpl<>(List.of(new AccessRequest())));
        var page = service.filter(USER_ID, filter, pageable);
        assertEquals(1, page.getSize());
        Specification<AccessRequest> captured = specCap.getValue();
        assertNotNull(captured);
        verify(accessRequestRepository, times(1)).findAll(eq(captured), eq(pageable));
    }
}
