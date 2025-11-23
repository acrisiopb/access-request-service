package com.acrisio.accesscontrol.service;

import com.acrisio.accesscontrol.api.dto.ModuleDTO;
import com.acrisio.accesscontrol.domain.enums.Department;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.repository.ModuleRepository;
import com.acrisio.accesscontrol.exception.EntityNotFoundException;
import com.acrisio.accesscontrol.exception.UnprocessableEntityException;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleServiceUnitTests {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private InternationalizationUtil message;

    @InjectMocks
    private ModuleService moduleService;

    private ModuleDTO validModuleDTO;
    private Module validModule;

    private static final Long MODULE_ID = 1L;
    private static final String MODULE_NAME = "TEST_MODULE";
    private static final String MODULE_DESC = "Test description";
    private static final Set<String> PERMITTED_DEPARTMENTS = Set.of("TI", "FINANCE");

    @BeforeEach
    void setUp() {

        lenient().when(message.getMessage(eq("Module.notfound"))).thenReturn("Module not found.");
        lenient().when(message.getMessage(eq("Module.name"))).thenReturn("Module name is required.");
        lenient().when(message.getMessage(eq("Module.description"))).thenReturn("Module description is required.");
        lenient().when(message.getMessage(eq("Module.active"))).thenReturn("Module active status is required.");
        lenient().when(message.getMessage(eq("Department.User"))).thenReturn("Department is required.");

        Set<Department> departments = PERMITTED_DEPARTMENTS.stream()
                .map(Department::valueOf)
                .collect(Collectors.toSet());

        validModule = Module.builder()
                .id(MODULE_ID)
                .name(MODULE_NAME)
                .description(MODULE_DESC)
                .active(true)
                .permittedDepartments(departments)
                .incompatibleModules(Set.of())
                .build();

        validModuleDTO = new ModuleDTO(
                MODULE_ID,
                MODULE_NAME,
                MODULE_DESC,
                true,
                PERMITTED_DEPARTMENTS,
                Set.of()
        );
    }

    @Test
    void createModule_Success() {

        ModuleDTO createDto = new ModuleDTO(null, MODULE_NAME, MODULE_DESC, true, PERMITTED_DEPARTMENTS, Set.of());

        org.mockito.ArgumentCaptor<Module> moduleCaptor = org.mockito.ArgumentCaptor.forClass(Module.class);
        when(moduleRepository.save(moduleCaptor.capture())).thenAnswer(invocation -> {
            Module module = moduleCaptor.getValue();
            module.setId(MODULE_ID);
            return module;
        });

        ModuleDTO result = moduleService.create(createDto);

        assertNotNull(result);
        assertEquals(MODULE_ID, result.id());

        Module savedModule = moduleCaptor.getValue();
        assertEquals(MODULE_NAME, savedModule.getName());
        assertEquals(MODULE_DESC, savedModule.getDescription());
        assertEquals(true, savedModule.getActive());
        assertEquals(2, savedModule.getPermittedDepartments().size());
        verify(moduleRepository, times(1)).save(eq(savedModule));
    }

    @Test
    void createModule_NameIsBlank_ThrowsUnprocessableEntityException() {

        ModuleDTO invalidDto = new ModuleDTO(null, " ", MODULE_DESC, true, PERMITTED_DEPARTMENTS, Set.of());
        // A mensagem de erro será mockada por lenient().when no setUp

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
                moduleService.create(invalidDto));

        assertEquals("Module name is required.", exception.getMessage());
        verify(message, times(1)).getMessage(eq("Module.name")); // Verificação de uso específico
        verifyNoInteractions(moduleRepository);
    }

    @Test
    void createModule_DescriptionIsBlank_ThrowsUnprocessableEntityException() {

        ModuleDTO invalidDto = new ModuleDTO(null, MODULE_NAME, " ", true, PERMITTED_DEPARTMENTS, Set.of());

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
                moduleService.create(invalidDto));

        assertEquals("Module description is required.", exception.getMessage());
        verify(message, times(1)).getMessage(eq("Module.description"));
        verifyNoInteractions(moduleRepository);
    }

    @Test
    void createModule_ActiveIsNull_ThrowsUnprocessableEntityException() {

        ModuleDTO invalidDto = new ModuleDTO(null, MODULE_NAME, MODULE_DESC, null, PERMITTED_DEPARTMENTS, Set.of());

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
                moduleService.create(invalidDto));

        assertEquals("Module active status is required.", exception.getMessage());
        verify(message, times(1)).getMessage(eq("Module.active"));
        verifyNoInteractions(moduleRepository);
    }

    @Test
    void createModule_PermittedDepartmentsIsEmpty_ThrowsUnprocessableEntityException() {

        ModuleDTO invalidDto = new ModuleDTO(null, MODULE_NAME, MODULE_DESC, true, Set.of(), Set.of());

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
                moduleService.create(invalidDto));

        assertEquals("Department is required.", exception.getMessage());
        verify(message, times(1)).getMessage(eq("Department.User"));
        verifyNoInteractions(moduleRepository);
    }


    @Test
    void findAll_ReturnsListOfModules() {

        Module module2 = Module.builder().id(2L).name("MOD_2").description("Desc 2").active(false).permittedDepartments(Set.of(Department.RH)).incompatibleModules(Set.of()).build();
        List<Module> modules = List.of(validModule, module2);
        when(moduleRepository.findAll()).thenReturn(modules);

        List<ModuleDTO> result = moduleService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(moduleRepository, times(1)).findAll();
    }

    @Test
    void findById_ModuleExists_ReturnsModuleDTO() {

        when(moduleRepository.findById(eq(MODULE_ID))).thenReturn(Optional.of(validModule));

        ModuleDTO result = moduleService.findById(MODULE_ID);

        assertNotNull(result);
        assertEquals(MODULE_ID, result.id());
        verify(moduleRepository, times(1)).findById(eq(MODULE_ID));
    }

    @Test
    void findById_ModuleDoesNotExist_ThrowsEntityNotFoundException() {

        when(moduleRepository.findById(eq(MODULE_ID))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                moduleService.findById(MODULE_ID));

        assertEquals("Module not found.", exception.getMessage());
        verify(moduleRepository, times(1)).findById(eq(MODULE_ID));
        verify(message, times(1)).getMessage(eq("Module.notfound"));
    }

    @Test
    void updateModule_Success() {

        Long updateId = MODULE_ID;
        String newName = "UPDATED_NAME";
        Set<String> newDepartments = Set.of("OTHER");
        ModuleDTO updateDto = new ModuleDTO(
                updateId,
                newName,
                "Updated description",
                false,
                newDepartments,
                Set.of()
        );

        when(moduleRepository.findById(eq(updateId))).thenReturn(Optional.of(validModule));
        org.mockito.ArgumentCaptor<Module> updateCaptor = org.mockito.ArgumentCaptor.forClass(Module.class);
        when(moduleRepository.save(updateCaptor.capture())).thenAnswer(i -> updateCaptor.getValue());

        ModuleDTO result = moduleService.update(updateDto);

        assertNotNull(result);
        assertEquals(newName, result.name());

        Module updatedModule = updateCaptor.getValue();
        assertEquals(updateId, updatedModule.getId());
        assertEquals(newName, updatedModule.getName());
        assertEquals(false, updatedModule.getActive());
        assertTrue(updatedModule.getPermittedDepartments().contains(Department.OTHER));
        verify(moduleRepository, times(1)).save(eq(updatedModule));
        verify(moduleRepository, times(1)).findById(eq(updateId));
    }

    @Test
    void updateModule_ModuleNotFound_ThrowsEntityNotFoundException() {

        when(moduleRepository.findById(eq(MODULE_ID))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                moduleService.update(validModuleDTO));

        assertEquals("Module not found.", exception.getMessage());
        verify(moduleRepository, times(1)).findById(eq(MODULE_ID));
        verify(message, times(1)).getMessage(eq("Module.notfound"));
        verify(moduleRepository, times(1)).findById(eq(MODULE_ID));
        verifyNoMoreInteractions(moduleRepository);
    }

    @Test
    void updateModule_NameIsBlank_ThrowsUnprocessableEntityException() {

        ModuleDTO invalidDto = new ModuleDTO(MODULE_ID, " ", MODULE_DESC, true, PERMITTED_DEPARTMENTS, Set.of());
        when(moduleRepository.findById(eq(MODULE_ID))).thenReturn(Optional.of(validModule));

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
                moduleService.update(invalidDto));

        assertEquals("Module name is required.", exception.getMessage());
        verify(message, times(1)).getMessage(eq("Module.name"));
        verify(moduleRepository, times(1)).findById(eq(MODULE_ID));
        verifyNoMoreInteractions(moduleRepository);
    }


    @Test
    void deleteModule_Success() {

        when(moduleRepository.existsById(eq(MODULE_ID))).thenReturn(true);
        doNothing().when(moduleRepository).deleteById(eq(MODULE_ID));

        moduleService.delete(MODULE_ID);

        verify(moduleRepository, times(1)).existsById(eq(MODULE_ID));
        verify(moduleRepository, times(1)).deleteById(eq(MODULE_ID));
    }

    @Test
    void deleteModule_ModuleNotFound_ThrowsEntityNotFoundException() {

        when(moduleRepository.existsById(eq(MODULE_ID))).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                moduleService.delete(MODULE_ID));

        assertEquals("Module not found.", exception.getMessage());
        verify(moduleRepository, times(1)).existsById(eq(MODULE_ID));
        verify(moduleRepository, never()).deleteById(eq(MODULE_ID));
        verify(message, times(1)).getMessage(eq("Module.notfound"));
    }
}