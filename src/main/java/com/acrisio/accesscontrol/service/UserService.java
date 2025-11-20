package com.acrisio.accesscontrol.service;

import com.acrisio.accesscontrol.api.dto.UserCreateDTO;
import com.acrisio.accesscontrol.api.dto.UserDTO;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.domain.repository.UserRepository;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final InternationalizationUtil message;

    @Transactional
    public UserDTO create(UserCreateDTO dto) {

        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException(message.getMessage("User.name"));
        }

        if (dto.email() == null || dto.email().isBlank()) {
            throw new IllegalArgumentException(message.getMessage("User.email"));
        }

        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException(message.getMessage("User.emailExists"));
        }

        if (dto.password() == null || dto.password().length() < 6) {
            throw new IllegalArgumentException(message.getMessage("User.password"));
        }

        if (dto.department() == null) {
            throw new IllegalArgumentException(message.getMessage("Department.User"));
        }

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setDepartment(dto.department());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));

        userRepository.save(user);

        return toDTO(user);
    }

    @Transactional
    public UserDTO update(UserDTO dto) {

        User user = userRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException(message.getMessage("User.notfound")));

        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setDepartment(dto.department());

        userRepository.save(user);
        return toDTO(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException(message.getMessage("User.notfound"));
        }
        userRepository.deleteById(id);
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }


    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(message.getMessage("User.notfound")));

        return toDTO(user);
    }


    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getDepartment()
        );
    }

}
