package com.acrisio.accesscontrol.service;

import com.acrisio.accesscontrol.api.dto.ModuleDTO;
import com.acrisio.accesscontrol.domain.enums.Department;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.repository.ModuleRepository;
import com.acrisio.accesscontrol.exception.EntityNotFoundException;
import com.acrisio.accesscontrol.exception.UnprocessableEntityException;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final InternationalizationUtil message;

    public ModuleDTO create(ModuleDTO dto) {

        if (dto.name().isBlank()) {
            throw new UnprocessableEntityException(message.getMessage("Module.name"));
        }
        if (dto.description().isBlank()) {
            throw new UnprocessableEntityException(message.getMessage("Module.description"));
        }
        if (dto.active() == null) {
            throw new UnprocessableEntityException(message.getMessage("Module.active"));
        }
        if (dto.permittedDepartments().isEmpty()) {
            throw new UnprocessableEntityException(message.getMessage("Department.User"));
        }

        Module module = new Module();
        module.setName(dto.name());
        module.setDescription(dto.description());
        module.setActive(dto.active());
        module.setPermittedDepartments(
                dto.permittedDepartments().stream()
                        .map(d -> Enum.valueOf(
                                com.acrisio.accesscontrol.domain.enums.Department.class,
                                d
                        ))
                        .collect(Collectors.toSet())
        );
        module.setIncompatibleModules(Set.of());

        moduleRepository.save(module);

        return toDTO(module);
    }

    public List<ModuleDTO> findAll() {
        return moduleRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ModuleDTO findById(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("Module.notfound")));
        return toDTO(module);
    }

    public ModuleDTO update(ModuleDTO dto) {
        Module module = moduleRepository.findById(dto.id())
                .orElseThrow(() -> new EntityNotFoundException(message.getMessage("Module.notfound")));
        if (dto.name().isBlank()) {
            throw new UnprocessableEntityException(message.getMessage("Module.name"));
        }
        if (dto.description().isBlank()) {
            throw new UnprocessableEntityException(message.getMessage("Module.description"));
        }
        if (dto.active() == null) {
            throw new UnprocessableEntityException(message.getMessage("Module.active"));
        }
        if (dto.permittedDepartments().isEmpty()) {
            throw new UnprocessableEntityException(message.getMessage("Department.User"));
        }

        module.setName(dto.name());
        module.setDescription(dto.description());
        module.setActive(dto.active());

        module.setPermittedDepartments(
                dto.permittedDepartments().stream()
                        .map(d -> Enum.valueOf(Department.class, d))
                        .collect(Collectors.toSet())
        );

        moduleRepository.save(module);
        return toDTO(module);
    }


    public void delete(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new EntityNotFoundException(message.getMessage("Module.notfound"));
        }
        moduleRepository.deleteById(id);
    }

    private ModuleDTO toDTO(Module m) {
        return new ModuleDTO(
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
        );
    }


}
