package com.acrisio.accesscontrol.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.acrisio.accesscontrol.domain.enums.HistoryAction;
import com.acrisio.accesscontrol.domain.model.*;
import com.acrisio.accesscontrol.domain.model.Module;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.api.dto.AccessRequestFilterDTO;
import com.acrisio.accesscontrol.api.dto.AccessRequestResponseDTO;
import com.acrisio.accesscontrol.api.dto.ModuleDTO;
import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import com.acrisio.accesscontrol.domain.repository.AccessRepositoy;
import com.acrisio.accesscontrol.domain.repository.AccessRequestRepository;
import com.acrisio.accesscontrol.domain.repository.ModuleRepository;
import com.acrisio.accesscontrol.domain.repository.ProtocolSequenceRepository;
import com.acrisio.accesscontrol.domain.repository.UserRepository;
import com.acrisio.accesscontrol.domain.rules.AccessRequestRule;
import com.acrisio.accesscontrol.exception.EntityNotFoundException;
import com.acrisio.accesscontrol.infrastructure.util.AccessRequestSpecification;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccessRequestService {

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final ProtocolSequenceRepository protocolSequenceRepository;
    private final AccessRepositoy accessRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final List<AccessRequestRule> rules;
    private final InternationalizationUtil message;
    private final com.acrisio.accesscontrol.domain.repository.RequestHistoryRepository requestHistoryRepository;

    @Transactional
    public AccessRequestResponseDTO createRequest(AccessRequestCreateDTO dto) {

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("User.notfound")));

        Set<Module> modules = loadModules(new HashSet<>(dto.moduleIds()));

        boolean approved = true;
        String denial = null;
        try {
            for (AccessRequestRule rule : rules) {
                rule.validate(user, modules, dto);
            }
        } catch (RuntimeException ex) {
            approved = false;
            denial = ex.getMessage();
        }

        AccessRequest request = new AccessRequest();
        request.setUser(user);
        request.setModules(modules);
        request.setJustification(dto.justification());
        request.setUrgent(dto.urgent());
        request.setCreatedAt(OffsetDateTime.now());
        request.setProtocol(generateProtocol());

        if (approved) {
            request.setStatus(RequestStatus.ACTIVE);
            request.setExpiresAt(OffsetDateTime.now().plusDays(180));
            createAccesses(user, modules);
        } else {
            request.setStatus(RequestStatus.DENIED);
            request.setDeniedReason(denial);
        }
        accessRequestRepository.save(request);
        return toResponseDTO(request);
    }

    public List<AccessRequestResponseDTO> findAll() {
        return accessRequestRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public AccessRequestResponseDTO findById(Long requestId, Long currentUserId) {

        AccessRequest req = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("AccessRequest.notfound")));

        if (!req.getUser().getId().equals(currentUserId)) {
            throw new EntityNotFoundException(message.getMessage("AccessRequest.notfound"));
        }

        return toResponseDTO(req);
    }


    public List<AccessRequestResponseDTO> listByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(message.getMessage("User.notfound")));

        return accessRequestRepository.findByUser(user)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public AccessRequestResponseDTO cancel(Long requestId, Long currentUserId, String reason) {

        // Buscar a solicitação
        AccessRequest req = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("AccessRequest.notfound")));

        //  Validar que pertence ao usuário autenticado
        if (!req.getUser().getId().equals(currentUserId)) {
            throw new EntityNotFoundException(message.getMessage("AccessRequest.notfound"));
        }

        // Validar que está ATIVO
        if (req.getStatus() != RequestStatus.ACTIVE) {
            throw new IllegalArgumentException(message.getMessage("AccessRequest.active.cancelled"));
        }

        // Validar motivo do cancelamento
        if (reason == null || reason.trim().length() < 10 || reason.trim().length() > 200) {
            throw new IllegalArgumentException(message.getMessage("AccessRequst.reason"));
        }

        // Atualizar a solicitação para CANCELADA
        req.setStatus(RequestStatus.CANCELED);
        req.setDeniedReason(reason);
        req.setExpiresAt(null);

        // Revogar acessos vinculados
        User user = req.getUser();
        Set<Module> modules = req.getModules();

        for (Access access : user.getActiveAccesses()) {
            if (modules.contains(access.getModule())) {
                accessRepository.delete(access);
            }
        }

        // Registrar histórico
        RequestHistory history = RequestHistory.builder()
                .accessRequest(req)
                .action(HistoryAction.CANCELED)
                .description(reason)
                .date(OffsetDateTime.now())
                .build();

        requestHistoryRepository.save(history);
        accessRequestRepository.save(req);

        return toResponseDTO(req);
    }


    @Transactional
    public void delete(Long id) {
        AccessRequest req = accessRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("AccessRequest.notfound")));

        accessRequestRepository.delete(req);
    }

    private Set<Module> loadModules(Set<Long> moduleIds) {
        return moduleIds.stream()
                .map(id -> moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("Module.notfound") + " ID = " + id)))
                .collect(Collectors.toSet());
    }

    private void createAccesses(User user, Set<Module> modules) {
        for (Module m : modules) {
            Access access = Access.builder()
                    .user(user)
                    .module(m)
                    .grantedAt(OffsetDateTime.now())
                    .expiresAt(OffsetDateTime.now().plusDays(180))
                    .build();

            accessRepository.save(access);
        }
    }

    private boolean approve(User user, Set<Module> modules) {
        return true; // Se nenhuma regra lançar exceção → aprovado
    }

    @Transactional
    public String generateProtocol() {

        String today = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        ProtocolSequence seq = protocolSequenceRepository
                .findById(today)
                .orElseGet(() -> new ProtocolSequence(today, 0));

        int next = seq.getCounter() + 1;
        seq.setCounter(next);

        protocolSequenceRepository.save(seq);

        String sequence = String.format("%04d", next);

        return "SOL-" + today + "-" + sequence;
    }

    private AccessRequestResponseDTO toResponseDTO(AccessRequest request) {
        return new AccessRequestResponseDTO(
                request.getId(),
                request.getProtocol(),
                request.getStatus(),
                request.getJustification(),
                request.getUrgent(),
                request.getCreatedAt(),
                request.getExpiresAt(),
                request.getDeniedReason(),
                request.getModules()
                        .stream()
                        .map(m -> new ModuleDTO(
                        m.getId(),
                        m.getName(),
                        m.getDescription(),
                        m.getActive(),
                        m.getPermittedDepartments().stream()
                                .map(Enum::name)
                                .collect(Collectors.toSet()),
                        m.getIncompatibleModules().stream()
                                .map(Module::getName)
                                .collect(Collectors.toSet())
                ))
                        .toList(),
                request.getHistory() == null ? java.util.List.of() : request.getHistory().stream()
                        .map(h -> new com.acrisio.accesscontrol.api.dto.RequestHistoryDTO(
                                h.getAction(),
                                h.getDescription(),
                                h.getDate()
                        ))
                        .toList()
        );
    }

    @Transactional
    public AccessRequestResponseDTO renew(Long requestId) {

        AccessRequest oldRequest = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("AccessRequest.notfound")));

        User user = oldRequest.getUser();
        if (oldRequest.getStatus() == RequestStatus.CANCELED) {
            throw new IllegalArgumentException("AccessRequest.denied.renewd");
        }

        if (oldRequest.getStatus() == RequestStatus.DENIED) {
            throw new IllegalArgumentException(message.getMessage("AccessRequest.cancelled"));
        }

        // Regra 1 — Só solicitações ATIVAS podem ser renovadas
        if (oldRequest.getStatus() != RequestStatus.ACTIVE) {
            throw new IllegalArgumentException(message.getMessage("AccessRequest.renew.cancelled"));
        }

        // Regra 2 — Só pode renovar quando faltarem menos de 30 dias
        if (oldRequest.getExpiresAt().isAfter(OffsetDateTime.now().plusDays(30))) {
            throw new IllegalArgumentException(message.getMessage("AccessRequest.info.renew"));
        }

        // Regra 3 — A renovação é uma NOVA solicitação
        AccessRequest newRequest = new AccessRequest();
        newRequest.setUser(user);
        newRequest.setModules(oldRequest.getModules());
        newRequest.setJustification(message.getMessage("AccessRequest.renew") + " " + oldRequest.getProtocol());
        newRequest.setUrgent(false);
        newRequest.setCreatedAt(OffsetDateTime.now());
        newRequest.setOriginRequest(oldRequest); // vincula a antiga
        newRequest.setProtocol(generateProtocol());

        // Regras de negócio novamente
        Set<Module> modules = oldRequest.getModules();
        AccessRequestCreateDTO dto = new AccessRequestCreateDTO(
                user.getId(),
                modules.stream().map(Module::getId).toList(),
                newRequest.getJustification(),
                false
        );

        for (AccessRequestRule rule : rules) {
            rule.validate(user, modules, dto);
        }

        // Se passou nas regras — aprovado
        newRequest.setStatus(RequestStatus.ACTIVE);
        newRequest.setExpiresAt(OffsetDateTime.now().plusDays(180));

        // Grava nova solicitação
        accessRequestRepository.save(newRequest);

        // Extende o acesso existente +180 dias
        for (Access access : user.getActiveAccesses()) {
            if (modules.contains(access.getModule())) {
                access.setExpiresAt(OffsetDateTime.now().plusDays(180));
                accessRepository.save(access);
            }
        }

        return toResponseDTO(newRequest);
    }

    public Page<AccessRequestResponseDTO> filter(Long userId, AccessRequestFilterDTO filter, Pageable pageable) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("User.notfound")));

        var spec = AccessRequestSpecification.filter(filter)
                .and((root, query, cb) -> cb.equal(root.get("user"), user));

        return accessRequestRepository.findAll(spec, pageable)
                .map(this::toResponseDTO);
    }

}
