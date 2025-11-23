package com.acrisio.accesscontrol.service;

import com.acrisio.accesscontrol.api.dto.UserCreateDTO;
import com.acrisio.accesscontrol.api.dto.UserDTO;
import com.acrisio.accesscontrol.domain.enums.Department;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.domain.repository.UserRepository;
import com.acrisio.accesscontrol.exception.EntityNotFoundException;
import com.acrisio.accesscontrol.exception.NameUniqueViolationException;
import com.acrisio.accesscontrol.exception.UnprocessableEntityException;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private InternationalizationUtil message;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void init() {
        lenient().when(message.getMessage(eq("User.name"))).thenReturn("Name is required.");
        lenient().when(message.getMessage(eq("User.email"))).thenReturn("Email is required.");
        lenient().when(message.getMessage(eq("User.emailExists"))).thenReturn("Email already exists.");
        lenient().when(message.getMessage(eq("User.password"))).thenReturn("Invalid password.");
        lenient().when(message.getMessage(eq("Department.User"))).thenReturn("Department is required.");
        lenient().when(message.getMessage(eq("User.notfound"))).thenReturn("User not found.");
        lenient().when(passwordEncoder.encode(eq("secret123"))).thenReturn("hashed");
    }

    @Test
    void create_Success() {
        UserCreateDTO dto = new UserCreateDTO("Alice", "alice@corp.com", Department.TI, "secret123");

        org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor.forClass(User.class);
        when(userRepository.existsByEmail(eq(dto.email()))).thenReturn(false);
        when(passwordEncoder.encode(eq("secret123"))).thenReturn("hashed");
        when(userRepository.save(captor.capture())).thenAnswer(i -> captor.getValue());

        UserDTO result = userService.create(dto);
        assertNotNull(result);
        User saved = captor.getValue();
        assertEquals("Alice", saved.getName());
        assertEquals("alice@corp.com", saved.getEmail());
        assertEquals("hashed", saved.getPasswordHash());
        verify(userRepository, times(1)).save(eq(saved));
    }

    @Test
    void create_NameBlank_Throws() {
        UserCreateDTO dto = new UserCreateDTO(" ", "a@b.com", Department.TI, "secret123");
        assertThrows(NameUniqueViolationException.class, () -> userService.create(dto));
        verifyNoInteractions(userRepository);
    }

    @Test
    void create_EmailBlank_Throws() {
        UserCreateDTO dto = new UserCreateDTO("Alice", " ", Department.TI, "secret123");
        assertThrows(NameUniqueViolationException.class, () -> userService.create(dto));
        verifyNoInteractions(userRepository);
    }

    @Test
    void create_EmailExists_Throws() {
        UserCreateDTO dto = new UserCreateDTO("Alice", "alice@corp.com", Department.TI, "secret123");
        when(userRepository.existsByEmail(eq(dto.email()))).thenReturn(true);
        assertThrows(NameUniqueViolationException.class, () -> userService.create(dto));
        verify(userRepository, times(1)).existsByEmail(eq(dto.email()));
    }

    @Test
    void create_PasswordTooShort_Throws() {
        UserCreateDTO dto = new UserCreateDTO("Alice", "alice@corp.com", Department.TI, "123");
        when(userRepository.existsByEmail(eq(dto.email()))).thenReturn(false);
        assertThrows(UnprocessableEntityException.class, () -> userService.create(dto));
        verify(userRepository, times(1)).existsByEmail(eq(dto.email()));
    }

    @Test
    void create_DepartmentNull_Throws() {
        UserCreateDTO dto = new UserCreateDTO("Alice", "alice@corp.com", null, "secret123");
        when(userRepository.existsByEmail(eq(dto.email()))).thenReturn(false);
        assertThrows(UnprocessableEntityException.class, () -> userService.create(dto));
        verify(userRepository, times(1)).existsByEmail(eq(dto.email()));
    }

    @Test
    void update_Success() {
        User existing = User.builder().id(USER_ID).name("Old").email("old@corp.com").department(Department.RH).build();
        UserDTO dto = new UserDTO(USER_ID, "New", "new@corp.com", Department.FINANCE);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail(eq(dto.email()))).thenReturn(false);

        org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(i -> captor.getValue());

        UserDTO res = userService.update(dto);
        assertNotNull(res);
        User saved = captor.getValue();
        assertEquals("New", saved.getName());
        assertEquals("new@corp.com", saved.getEmail());
        assertEquals(Department.FINANCE, saved.getDepartment());
        verify(userRepository, times(1)).save(eq(saved));
    }

    @Test
    void update_NotFound_Throws() {
        UserDTO dto = new UserDTO(USER_ID, "New", "new@corp.com", Department.FINANCE);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.update(dto));
        verify(userRepository, times(1)).findById(eq(USER_ID));
    }

    @Test
    void update_NameBlank_Throws() {
        User existing = User.builder().id(USER_ID).name("Old").email("old@corp.com").department(Department.RH).build();
        UserDTO dto = new UserDTO(USER_ID, " ", "new@corp.com", Department.FINANCE);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(existing));
        assertThrows(NameUniqueViolationException.class, () -> userService.update(dto));
    }

    @Test
    void update_EmailBlank_Throws() {
        User existing = User.builder().id(USER_ID).name("Old").email("old@corp.com").department(Department.RH).build();
        UserDTO dto = new UserDTO(USER_ID, "New", " ", Department.FINANCE);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(existing));
        assertThrows(NameUniqueViolationException.class, () -> userService.update(dto));
    }

    @Test
    void update_EmailExists_Throws() {
        User existing = User.builder().id(USER_ID).name("Old").email("old@corp.com").department(Department.RH).build();
        UserDTO dto = new UserDTO(USER_ID, "New", "dup@corp.com", Department.FINANCE);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail(eq(dto.email()))).thenReturn(true);
        assertThrows(NameUniqueViolationException.class, () -> userService.update(dto));
        verify(userRepository, times(1)).existsByEmail(eq(dto.email()));
    }

    @Test
    void update_DepartmentNull_Throws() {
        User existing = User.builder().id(USER_ID).name("Old").email("old@corp.com").department(Department.RH).build();
        UserDTO dto = new UserDTO(USER_ID, "New", "new@corp.com", null);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(existing));
        assertThrows(UnprocessableEntityException.class, () -> userService.update(dto));
    }

    @Test
    void delete_Success() {
        when(userRepository.existsById(eq(USER_ID))).thenReturn(true);
        doNothing().when(userRepository).deleteById(eq(USER_ID));
        userService.delete(USER_ID);
        verify(userRepository, times(1)).deleteById(eq(USER_ID));
    }

    @Test
    void delete_NotFound_Throws() {
        when(userRepository.existsById(eq(USER_ID))).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> userService.delete(USER_ID));
        verify(userRepository, times(1)).existsById(eq(USER_ID));
    }

    @Test
    void findAll_ReturnsList() {
        User u1 = User.builder().id(1L).name("A").email("a@b.com").department(Department.TI).build();
        User u2 = User.builder().id(2L).name("B").email("b@c.com").department(Department.RH).build();
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));
        List<UserDTO> res = userService.findAll();
        assertEquals(2, res.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findById_Success() {
        User u1 = User.builder().id(USER_ID).name("A").email("a@b.com").department(Department.TI).build();
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(u1));
        UserDTO res = userService.findById(USER_ID);
        assertEquals(USER_ID, res.id());
        verify(userRepository, times(1)).findById(eq(USER_ID));
    }

    @Test
    void findById_NotFound_Throws() {
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.findById(USER_ID));
        verify(userRepository, times(1)).findById(eq(USER_ID));
    }
}