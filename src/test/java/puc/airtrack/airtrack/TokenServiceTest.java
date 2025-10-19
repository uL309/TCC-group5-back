package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.interfaces.DecodedJWT;

import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserRole;

public class TokenServiceTest {

    private TokenService service;

    @BeforeEach
    void setup() {
        service = new TokenService();
        // injeta secret no campo private
        ReflectionTestUtils.setField(service, "secret", "unit-test-secret-very-secret");
    }

    @Test
    void generateToken_validateAndDecode_success() {
        User u = new User();
        u.setUsername("tester");
        u.setRole(UserRole.ROLE_ENGENHEIRO);
        u.setFirstAccess(Boolean.TRUE);
        u.setCpf("12345678900");
        u.setId(42);

        String token = service.generateToken(u);
        assertNotNull(token);

        String subject = service.validateToken(token);
        assertEquals("tester", subject);

        DecodedJWT decoded = service.decodeToken(token);
        assertNotNull(decoded);
        assertEquals("tester", decoded.getSubject());
        assertEquals(42, decoded.getClaim("id").asInt());
        assertEquals("12345678900", decoded.getClaim("cpf").asString());
        assertTrue(decoded.getExpiresAt().getTime() > System.currentTimeMillis());
    }

    @Test
    void validateToken_tamperedToken_returnsNull() {
        User u = new User();
        u.setUsername("x");
        u.setRole(puc.airtrack.airtrack.Login.UserRole.ROLE_ENGENHEIRO); // evita NPE
        u.setFirstAccess(Boolean.FALSE);
        u.setCpf("00000000000");
        ReflectionTestUtils.setField(service, "secret", "another-secret");
        u.setId(1);

        String token = service.generateToken(u);
        // tamper token (change a char) to invalidate signature
        String tampered = token.substring(0, token.length() - 1) + (token.charAt(token.length() - 1) == 'a' ? 'b' : 'a');

        assertNull(service.validateToken(tampered));
        assertNull(service.decodeToken(tampered));
    }

    @Test
    void validateToken_differentSecretAfterGeneration_returnsNull() {
        User u = new User();
        u.setUsername("userA");
        u.setRole(puc.airtrack.airtrack.Login.UserRole.ROLE_ENGENHEIRO); // evita NPE
        u.setId(7);

        // generate with current secret
        String token = service.generateToken(u);

        // change secret to simulate verification with wrong key
        ReflectionTestUtils.setField(service, "secret", "wrong-secret-now");
        assertNull(service.validateToken(token));
        assertNull(service.decodeToken(token));
    }

    @Test
    void generateToken_withNullSecret_throws() {
        User u = new User();
        u.setUsername("z");
        // set secret to null -> Algorithm.HMAC256 will throw IllegalArgumentException
        ReflectionTestUtils.setField(service, "secret", null);
        assertThrows(IllegalArgumentException.class, () -> service.generateToken(u));
    }
}
