package puc.airtrack.airtrack.Filters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.FilterChain;
import puc.airtrack.airtrack.SecurityFilter;
import puc.airtrack.airtrack.TokenService;

@ExtendWith(MockitoExtension.class)
public class SecurityFilterTeste {

    @InjectMocks
    private SecurityFilter securityFilter;

    @Mock
    private TokenService tokenService;

    @Mock
    private puc.airtrack.airtrack.Login.UserService userService;

    @Mock
    private DecodedJWT decodedJWT;

    @Mock
    private Claim roleClaim;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void cleanup() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_noAuthorizationHeader_callsChain_and_noAuthentication() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        securityFilter.doFilter(req, res, filterChain);

        verify(tokenService, never()).decodeToken(anyString());
        verify(filterChain).doFilter(req, res);
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void doFilter_tokenDecodeReturnsNull_callsChain_and_noAuthentication() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer sometoken");
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(tokenService.decodeToken("sometoken")).thenReturn(null);

        securityFilter.doFilter(req, res, filterChain);

        verify(tokenService).decodeToken("sometoken");
        verify(filterChain).doFilter(req, res);
        assertNull(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_validToken_setsAuthentication_and_callsChain() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer goodtoken");
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(tokenService.decodeToken("goodtoken")).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn("userA");
        when(decodedJWT.getClaim("role")).thenReturn(roleClaim);
        when(roleClaim.asString()).thenReturn("ROLE_USER");

        // devolver o tipo esperado pelo seu UserService (sua entidade User)
        puc.airtrack.airtrack.Login.User appUser = mock(puc.airtrack.airtrack.Login.User.class);
        when(appUser.getUsername()).thenReturn("userA");
        when(userService.findByUsername("userA")).thenReturn(appUser);

        securityFilter.doFilter(req, res, filterChain);

        verify(tokenService).decodeToken("goodtoken");
        verify(userService).findByUsername("userA");
        verify(filterChain).doFilter(req, res);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        // não assumir estrutura de authorities aqui (depende de como seu filtro monta a Authentication)
        assertEquals("userA", org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilter_missingSubjectOrRole_doesNotSetAuthentication() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer partialtoken");
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(tokenService.decodeToken("partialtoken")).thenReturn(decodedJWT);
        when(decodedJWT.getSubject()).thenReturn(null); // missing subject
        when(decodedJWT.getClaim("role")).thenReturn(roleClaim);
        when(roleClaim.asString()).thenReturn("ROLE_USER");

        securityFilter.doFilter(req, res, filterChain);

        verify(tokenService).decodeToken("partialtoken");
        verify(filterChain).doFilter(req, res);
        assertNull(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
    }
}