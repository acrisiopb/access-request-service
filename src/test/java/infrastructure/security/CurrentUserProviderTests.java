package infrastructure.security;

import com.acrisio.accesscontrol.domain.model.User;
import com.acrisio.accesscontrol.domain.repository.UserRepository;
import com.acrisio.accesscontrol.infrastructure.security.CurrentUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserProviderTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CurrentUserProvider currentUserProvider;

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "test@example.com";
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(USER_ID).email(USER_EMAIL).name("Test User").build();

        // Configura o SecurityContextHolder para simular o estado autenticado
        SecurityContextHolder.setContext(securityContext);

        // FIX: Usar lenient() para evitar UnnecessaryStubbingException, mas manter o setup.
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(USER_EMAIL);
    }

    @AfterEach
    void tearDown() {
        // Limpa o contexto após cada teste para evitar efeitos colaterais (bleed-over)
        SecurityContextHolder.clearContext();
    }

    @Test
    void get_AuthenticatedUserExists_ReturnsUser() {
        // Arrange
        when(userRepository.findByEmail(eq(USER_EMAIL))).thenReturn(Optional.of(mockUser));
        // O stub authentication.getName() já está no setUp.

        // Act
        User result = currentUserProvider.get();

        // Assert
        assertNotNull(result);
        assertEquals(USER_ID, result.getId());

        verify(userRepository, times(1)).findByEmail(eq(USER_EMAIL));
        // FIX: A chamada é duplicada no código de produção (if + findByEmail), então esperamos times(2)
        verify(authentication, times(2)).getName();
    }

    @Test
    void get_NoAuthentication_ThrowsIllegalStateException() {
        // Arrange
        // Simula a falta de autenticação
        SecurityContextHolder.clearContext();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                currentUserProvider.get());

        assertEquals("Usuário não autenticado", exception.getMessage());
        verifyNoInteractions(userRepository);
        // A autenticação é verificada, mas o getAuthentication pode retornar null, evitando outras chamadas.
    }

    @Test
    void get_UserEmailIsNull_ThrowsIllegalStateException() {
        // Arrange
        when(authentication.getName()).thenReturn(null); // Simula nome (e-mail) nulo

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                currentUserProvider.get());

        assertEquals("Usuário não autenticado", exception.getMessage());
        verifyNoInteractions(userRepository);
        verify(authentication, times(1)).getName(); // Chamado apenas na linha 20 do if
    }

    @Test
    void get_AuthenticationPresentUserNotFound_ThrowsIllegalStateException() {
        // Arrange
        when(userRepository.findByEmail(eq(USER_EMAIL))).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                currentUserProvider.get());

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(eq(USER_EMAIL));
        verify(authentication, times(2)).getName();
    }
}