package com.acrisio.accesscontrol.service;

import com.acrisio.accesscontrol.api.dto.AuthLoginRequest;
import com.acrisio.accesscontrol.api.dto.AuthResponseDTO;
import com.acrisio.accesscontrol.domain.enums.Department;
import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.domain.repository.UserRepository;
import com.acrisio.accesscontrol.infrastructure.security.JwtTokenProvider;
import com.acrisio.accesscontrol.infrastructure.util.InternationalizationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private InternationalizationUtil message;

    @InjectMocks
    private AuthService authService;

    private static final String VALID_EMAIL = "test@corp.com";
    private static final String VALID_PASSWORD = "password123";
    private static final String HASHED_PASSWORD = "hashedPassword";
    private static final String JWT_TOKEN = "mocked.jwt.token";
    private static final Long USER_ID = 1L;
    private static final Department USER_DEPT = Department.TI;
    private static final String INVALID_CREDENTIALS_MSG = "Invalid credentials.";

    private User validUser;
    private AuthLoginRequest validRequest;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .id(USER_ID)
                .name("Test User")
                .email(VALID_EMAIL)
                .passwordHash(HASHED_PASSWORD)
                .department(USER_DEPT)
                .build();

        validRequest = new AuthLoginRequest(VALID_EMAIL, VALID_PASSWORD);

        lenient().when(message.getMessage(eq("Auth.invalid"))).thenReturn(INVALID_CREDENTIALS_MSG);
    }

    @Test
    void login_ValidCredentials_ReturnsAuthResponseDTO() {
        OffsetDateTime expirationTime = OffsetDateTime.now().plusHours(2);

        when(userRepository.findByEmail(eq(VALID_EMAIL))).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(eq(VALID_PASSWORD), eq(HASHED_PASSWORD))).thenReturn(true);
        when(jwtTokenProvider.generateToken(eq(USER_ID), eq(VALID_EMAIL))).thenReturn(JWT_TOKEN);
        when(jwtTokenProvider.expirationFromNow()).thenReturn(expirationTime);

        AuthResponseDTO result = authService.login(validRequest);

        assertNotNull(result);

        verify(userRepository, times(1)).findByEmail(eq(VALID_EMAIL));
        verify(passwordEncoder, times(1)).matches(eq(VALID_PASSWORD), eq(HASHED_PASSWORD));
        verify(jwtTokenProvider, times(1)).generateToken(eq(USER_ID), eq(VALID_EMAIL));
        verify(jwtTokenProvider, times(1)).expirationFromNow();
    }

    @Test
    void login_UserNotFound_ThrowsIllegalArgumentException() {
        when(userRepository.findByEmail(eq(VALID_EMAIL))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authService.login(validRequest));

        assertEquals(INVALID_CREDENTIALS_MSG, exception.getMessage());

        verify(userRepository, times(1)).findByEmail(eq(VALID_EMAIL));
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtTokenProvider);
        verify(message, times(1)).getMessage(eq("Auth.invalid"));
    }

    @Test
    void login_InvalidPassword_ThrowsIllegalArgumentException() {
        when(userRepository.findByEmail(eq(VALID_EMAIL))).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(eq(VALID_PASSWORD), eq(HASHED_PASSWORD))).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authService.login(validRequest));

        assertEquals(INVALID_CREDENTIALS_MSG, exception.getMessage());

        verify(userRepository, times(1)).findByEmail(eq(VALID_EMAIL));
        verify(passwordEncoder, times(1)).matches(eq(VALID_PASSWORD), eq(HASHED_PASSWORD));
        verifyNoInteractions(jwtTokenProvider);
        verify(message, times(1)).getMessage(eq("Auth.invalid"));
    }
}