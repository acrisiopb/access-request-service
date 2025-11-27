package com.acrisio.accesscontrol.infrastructure.init;

import com.acrisio.accesscontrol.domain.enums.Department;
import com.acrisio.accesscontrol.domain.model.Module;
import com.acrisio.accesscontrol.domain.model.Access;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.domain.repository.AccessRepositoy;
import com.acrisio.accesscontrol.domain.repository.ModuleRepository;
import com.acrisio.accesscontrol.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatabaseSeederUnitTests {

    @Mock
    private ModuleRepository moduleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccessRepositoy accessRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DatabaseSeeder seeder;

    private List<Module> moduleList;

    @BeforeEach
    void setup() {
        moduleList = List.of(
                Module.builder().name("APROVADOR_FINANCEIRO").build(),
                Module.builder().name("SOLICITANTE_FINANCEIRO").build(),
                Module.builder().name("ADMINISTRADOR_RH").build(),
                Module.builder().name("COLABORADOR_RH").build(),
                Module.builder().name("ESTOQUE").build(),
                Module.builder().name("COMPRAS").build(),
                Module.builder().name("RELATORIOS").build(),
                Module.builder().name("GESTAO_FINANCEIRA").build()
        );

        when(passwordEncoder.encode(eq("alice123"))).thenReturn("hash");
        when(passwordEncoder.encode(eq("bruno123"))).thenReturn("hash");
        when(passwordEncoder.encode(eq("carla123"))).thenReturn("hash");
        when(passwordEncoder.encode(eq("diego123"))).thenReturn("hash");
        when(passwordEncoder.encode(eq("eva123"))).thenReturn("hash");
        when(passwordEncoder.encode(eq("test123"))).thenReturn("hash");

        when(userRepository.findByEmail("alice@corp.com")).thenReturn(Optional.of(User.builder().email("alice@corp.com").department(Department.TI).build()));
        when(userRepository.findByEmail("bruno@corp.com")).thenReturn(Optional.of(User.builder().email("bruno@corp.com").department(Department.FINANCE).build()));
        when(userRepository.findByEmail("carla@corp.com")).thenReturn(Optional.of(User.builder().email("carla@corp.com").department(Department.RH).build()));
        when(userRepository.findByEmail("diego@corp.com")).thenReturn(Optional.of(User.builder().email("diego@corp.com").department(Department.OPERATIONS).build()));
        when(userRepository.findByEmail("eva@corp.com")).thenReturn(Optional.of(User.builder().email("eva@corp.com").department(Department.OTHER).build()));
        when(userRepository.findByEmail("test@admin.com")).thenReturn(Optional.of(User.builder().email("test@admin.com").department(Department.TI).build()));
    }

    @Test
    void run_SeedsWhenEmpty() {
        when(moduleRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(accessRepository.count()).thenReturn(0L);

        when(moduleRepository.findAll()).thenReturn(moduleList);

        assertDoesNotThrow(() -> seeder.run(new DefaultApplicationArguments(new String[]{})));

        ArgumentCaptor<java.util.List> modCap = ArgumentCaptor.forClass(java.util.List.class);
        ArgumentCaptor<java.util.List> userCap = ArgumentCaptor.forClass(java.util.List.class);
        ArgumentCaptor<java.util.List> accessCap = ArgumentCaptor.forClass(java.util.List.class);

        verify(moduleRepository, atLeastOnce()).saveAll(modCap.capture());
        verify(userRepository, atLeastOnce()).saveAll(userCap.capture());
        verify(accessRepository, atLeastOnce()).saveAll(accessCap.capture());

        assertFalse(modCap.getValue().isEmpty());
        assertFalse(userCap.getValue().isEmpty());
        assertFalse(accessCap.getValue().isEmpty());
    }

    @Test
    void run_DoesNotSeedWhenNotEmpty() {
        when(moduleRepository.count()).thenReturn(5L);
        when(userRepository.count()).thenReturn(6L);
        when(accessRepository.count()).thenReturn(10L);

        assertDoesNotThrow(() -> seeder.run(new DefaultApplicationArguments(new String[]{})));

        verify(moduleRepository, atLeastOnce()).count();
        verify(userRepository, atLeastOnce()).count();
        verify(accessRepository, atLeastOnce()).count();
        verifyNoMoreInteractions(moduleRepository, userRepository, accessRepository);
    }
}

