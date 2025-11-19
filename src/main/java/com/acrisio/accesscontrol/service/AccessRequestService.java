package com.acrisio.accesscontrol.service;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.api.dto.AccessRequestResponseDTO;
import com.acrisio.accesscontrol.api.dto.ModuleDTO;
import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import com.acrisio.accesscontrol.domain.model.Access;
import com.acrisio.accesscontrol.domain.model.AccessRequest;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.domain.repository.AccessRepositoy;
import com.acrisio.accesscontrol.domain.repository.AccessRequestRepository;
import com.acrisio.accesscontrol.domain.repository.ModuleRepository;
import com.acrisio.accesscontrol.domain.repository.UserRepository;
import com.acrisio.accesscontrol.domain.rules.AccessRequestRule;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessRequestService {

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final AccessRepositoy accessRepository;
    private final AccessRequestRepository accessRequestRepository;

    private final List<AccessRequestRule> rules;

    @Transactional
    public AccessRequestResponseDTO createRequest(AccessRequestCreateDTO dto) {

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<Module> modules = loadModules(new HashSet<>(dto.moduleIds()));

        for (AccessRequestRule rule : rules) {
            rule.validate(user, modules, dto);
        }

        AccessRequest request = new AccessRequest();
        request.setUser(user);
        request.setModules(modules);
        request.setJustification(dto.justification());
        request.setUrgent(dto.urgent());
        request.setCreatedAt(OffsetDateTime.now());
        request.setProtocol(generateProtocol());

        boolean approved = approve(user, modules);

        if (approved) {
            request.setStatus(RequestStatus.ACTIVE);
            request.setExpiresAt(OffsetDateTime.now().plusDays(180));
            createAccesses(user, modules);
        } else {
            request.setStatus(RequestStatus.DENIED);
            request.setDeniedReason("Rule violation");
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

    public AccessRequestResponseDTO findById(Long id) {
        AccessRequest req = accessRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        return toResponseDTO(req);
    }

    public List<AccessRequestResponseDTO> listByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return accessRequestRepository.findByUser(user)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public AccessRequestResponseDTO cancel(Long requestId) {

        AccessRequest req = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (req.getStatus() == RequestStatus.DENIED) {
            throw new IllegalArgumentException("Denied requests cannot be canceled");
        }

        req.setStatus(RequestStatus.CANCELED);
        req.setDeniedReason("Request canceled by user");
        req.setExpiresAt(null);

        accessRequestRepository.save(req);

        return toResponseDTO(req);
    }


    @Transactional
    public void delete(Long id) {
        AccessRequest req = accessRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        accessRequestRepository.delete(req);
    }


    private Set<Module> loadModules(Set<Long> moduleIds) {
        return moduleIds.stream()
                .map(id -> moduleRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Module not found: " + id)))
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

    private String generateProtocol() {
        String date = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long number = System.currentTimeMillis() % 10000;
        return "SOL-" + date + "-" + number;
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
                        .toList()
        );
    }

    @Transactional
    public AccessRequestResponseDTO renew(Long id) {

        AccessRequest req = accessRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // Regras básicas
        if (req.getStatus() == RequestStatus.CANCELED) {
            throw new IllegalArgumentException("Canceled requests cannot be renewed");
        }

        if (req.getStatus() == RequestStatus.DENIED) {
            throw new IllegalArgumentException("Denied requests cannot be renewed");
        }

        // Renovação
        req.setExpiresAt(OffsetDateTime.now().plusDays(180));
        req.setStatus(RequestStatus.ACTIVE);

        accessRequestRepository.save(req);

        return toResponseDTO(req);
    }

}
