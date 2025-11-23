package infrastructure.security;

import com.acrisio.accesscontrol.infrastructure.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTests {

    // Configurações de teste
    private static final String TEST_SECRET = "test-secret-key-1234567890-abcdef-0123456789-XYZ987654321";
    private static final long EXPIRATION_MILLIS = 3600000; // 1 hour
    private static final Long USER_ID = 42L;
    private static final String USER_EMAIL = "test.user@corp.com";

    private final JwtTokenProvider provider = new JwtTokenProvider(TEST_SECRET, EXPIRATION_MILLIS);

    @Test
    void generateToken_ProducesValidJwtWithCorrectClaims() {
        // Act
        String token = provider.generateToken(USER_ID, USER_EMAIL);

        // Assert
        assertNotNull(token);

        // Verifica se o token é parsable e contém as claims corretas
        Claims claims = provider.parseClaims(token);
        assertEquals(String.valueOf(USER_ID), claims.getSubject());
        assertEquals(USER_EMAIL, claims.get("email", String.class));

        // Verifica o tempo de expiração
        long expectedExpiration = claims.getIssuedAt().getTime() + EXPIRATION_MILLIS;
        assertEquals(expectedExpiration, claims.getExpiration().getTime());
    }

    @Test
    void parseClaims_ThrowsExceptionForInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.structure";

        // Act & Assert
        assertThrows(Exception.class, () -> provider.parseClaims(invalidToken));
    }

    @Test
    void expirationFromNow_ReturnsCorrectOffsetDateTime() {
        // Act
        OffsetDateTime expiration = provider.expirationFromNow();
        OffsetDateTime expectedMinimum = OffsetDateTime.now().plusSeconds((EXPIRATION_MILLIS / 1000) - 1);

        // Assert
        assertNotNull(expiration);
        // Verifica se a expiração está aproximadamente 1 hora à frente (damos uma margem de 1 segundo)
        assertTrue(expiration.isAfter(expectedMinimum));
    }
}