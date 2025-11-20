package com.acrisio.accesscontrol.service;

import com.acrisio.accesscontrol.api.dto.AccessResponseDTO;
import com.acrisio.accesscontrol.domain.model.Access;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.domain.repository.AccessRepositoy;
import com.acrisio.accesscontrol.domain.repository.UserRepository;
import com.acrisio.accesscontrol.exception.EntityNotFoundException;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessService {

    private final AccessRepositoy accessRepository;
    private final UserRepository userRepository;
    private final InternationalizationUtil message;

    @Transactional
    public AccessResponseDTO revoke(Long id) {

        Access access = accessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("Access.notfound")));

        accessRepository.delete(access);
        return toDTO(access);
    }

    @Transactional
    public AccessResponseDTO renew(Long id) {

        Access access = accessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("Access.notfound")));

        access.setExpiresAt(access.getExpiresAt().plusDays(180));
        accessRepository.save(access);

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
