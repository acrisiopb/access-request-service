package com.acrisio.accesscontrol.service;

import com.acrisio.accesscontrol.api.dto.AccessRequestCreateDTO;
import com.acrisio.accesscontrol.api.dto.AccessResponseDTO;
import com.acrisio.accesscontrol.domain.enums.HistoryAction;
import com.acrisio.accesscontrol.domain.enums.RequestStatus;
import com.acrisio.accesscontrol.domain.model.Access;
import com.acrisio.accesscontrol.domain.model.AccessRequest;
import com.acrisio.accesscontrol.domain.model.RequestHistory;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.domain.repository.AccessRepositoy;
import com.acrisio.accesscontrol.domain.repository.AccessRequestRepository;
import com.acrisio.accesscontrol.domain.repository.RequestHistoryRepository;
import com.acrisio.accesscontrol.domain.repository.UserRepository;
import com.acrisio.accesscontrol.domain.rules.AccessRequestRule;
import com.acrisio.accesscontrol.exception.EntityNotFoundException;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccessService {

    private final AccessRepositoy accessRepository;
    private final AccessRequestRepository accessRequestRepository;
    private  final RequestHistoryRepository requestHistoryRepository;
    private final UserRepository userRepository;
    private final InternationalizationUtil message;
    private final AccessRequestService accessRequestService;
    private final List<AccessRequestRule> rules;

    @Transactional
    public AccessResponseDTO revoke(Long id) {

        Access access = accessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("Access.notfound")));

        accessRepository.delete(access);
        return toDTO(access);
    }

    @Transactional
    public AccessResponseDTO renew(Long accessId, Long currentUserId) {

        OffsetDateTime now = OffsetDateTime.now();

        Access access = accessRepository.findById(accessId)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("Access.notfound")));

        //Validar que pertence ao usuário autenticado
        if (!access.getUser().getId().equals(currentUserId)) {
            throw new EntityNotFoundException(message.getMessage("Access.notfound"));
        }

        //Validar que está ATIVO (pela data)
        if (access.getExpiresAt() == null || access.getExpiresAt().isBefore(now)) {
            throw new IllegalArgumentException(message.getMessage("Access.renew.active"));
        }

        //Validar regra dos 30 dias
        if (access.getExpiresAt().isAfter(now.plusDays(30))) {
            throw new IllegalArgumentException(message.getMessage("Access.info.renew"));
        }

        //Criar nova solicitação vinculada

        AccessRequest newReq = new AccessRequest();

        newReq.setUser(access.getUser());
        newReq.setModules(Set.of(access.getModule()));
        newReq.setJustification(message.getMessage("Access.renew") + " " +  access.getModule().getName());
        newReq.setUrgent(false);
        newReq.setCreatedAt(now);
        newReq.setProtocol(accessRequestService.generateProtocol());
        newReq.setOriginRequest(null);

        //Reaplicar Regras de Negócio

        boolean approved = true;
        String denial = null;

        try {
            AccessRequestCreateDTO dto = new AccessRequestCreateDTO(
                    access.getUser().getId(),
                    List.of(access.getModule().getId()),
                    newReq.getJustification(),
                    false);

            for (AccessRequestRule rule : rules) {
                rule.validate(access.getUser(), Set.of(access.getModule()), dto);
            }

        } catch (RuntimeException ex) {
            approved = false;
            denial = ex.getMessage();
        }

        if (approved) {
            // Solicitação aprovada
            newReq.setStatus(RequestStatus.ACTIVE);
            newReq.setExpiresAt(now.plusDays(180));

            // RENOVAR ACESSO + 180 dias
            access.setExpiresAt(access.getExpiresAt().plusDays(180));
            accessRepository.save(access);

        } else {
            newReq.setStatus(RequestStatus.DENIED);
            newReq.setDeniedReason(denial);
        }

        accessRequestRepository.save(newReq);

        //Registrar histórico
        if (approved) {
            //Registrar histórico
            RequestHistory history = RequestHistory.builder()
                    .accessRequest(newReq)
                    .action(HistoryAction.RENEWED)
                    .description("Renovação do acesso")
                    .date(now)
                    .build();

            requestHistoryRepository.save(history);
        }

        return toDTO(access);
    }

    public AccessResponseDTO findById(Long id) {
        Access access = accessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("Access.notfound")));

        return toDTO(access);
    }

    public List<AccessResponseDTO> findByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("Access.notfound")));

        return accessRepository.findByUser(user)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<AccessResponseDTO> findAll() {
        return accessRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private AccessResponseDTO toDTO(Access access) {
        return new AccessResponseDTO(
                access.getId(),
                access.getUser().getId(),
                access.getModule().getId(),
                access.getModule().getName(),
                access.getGrantedAt(),
                access.getExpiresAt()
        );
    }
}
