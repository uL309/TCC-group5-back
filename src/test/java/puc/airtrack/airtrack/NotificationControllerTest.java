package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import puc.airtrack.airtrack.notifications.Notification;
import puc.airtrack.airtrack.notifications.NotificationStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.notifications.NotificationController;
import puc.airtrack.airtrack.notifications.NotificationRepository;
import puc.airtrack.airtrack.notifications.NotificationStatus;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationRepository repo;

    // mock beans usados pelo security layer para evitar falha do ApplicationContext em @WebMvcTest
    @MockBean
    private puc.airtrack.airtrack.TokenService tokenService;
    
    // se sua aplicação registra um filtro named "securityFilter" (ou outro filtro), mocke-o também
    @MockBean(name = "securityFilter")
    private SecurityFilter securityFilter;
    
    // caso o próximo erro aponte AuthenticationManager/PasswordEncoder, adicione também:
    // @MockBean private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    // @MockBean private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void list_returnsPage_forAuthenticatedUser() throws Exception {
        // use uma instância concreta do User e id como int (se setId aceita int)
        User principal = new User();
        principal.setId(1);

        Notification n = new Notification();
        n.setId(10L);
        n.setUserId(1L);
        n.setStatus(NotificationStatus.ACTIVE);
        
        // use anyLong() para evitar mismatch int vs Long vindo do principal
        when(repo.findByUserIdAndStatusInOrderByCreatedAtDesc(anyLong(), anyList(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(n)));

        // garantir que o SecurityContext contenha a Authentication usada pelo controller
        var auth = new UsernamePasswordAuthenticationToken(principal, null);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            mockMvc.perform(get("/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].id").value(10));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }

        verify(repo).findByUserIdAndStatusInOrderByCreatedAtDesc(anyLong(), anyList(), any(Pageable.class));
    }

    @Test
    void markRead_success_changesStatus_whenOwner() throws Exception {
        User principal = new User();
        principal.setId(1);

        Notification n = new Notification();
        n.setId(5L);
        n.setUserId(1L);
        n.setStatus(NotificationStatus.ACTIVE);

        when(repo.findById(5L)).thenReturn(Optional.of(n));

        var auth = new UsernamePasswordAuthenticationToken(principal, null);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            mockMvc.perform(post("/notifications/5/read"))
                   .andExpect(status().isOk());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }

        // status deve ter sido alterado
        assert(n.getStatus() == NotificationStatus.READ);
        verify(repo).findById(5L);
    }

    @Test
    void markRead_throws_whenNotOwner() throws Exception {
        User principal = new User();
        principal.setId(1);

        Notification n = new Notification();
        n.setId(6L);
        n.setUserId(2L); // outro usuário
        n.setStatus(NotificationStatus.ACTIVE);

        when(repo.findById(6L)).thenReturn(Optional.of(n));

        var auth2 = new UsernamePasswordAuthenticationToken(principal, null);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth2);
        try {
            // o controller lança SecurityException -> MockMvc irá propagar como ServletException
            jakarta.servlet.ServletException ex = assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(post("/notifications/6/read"))
            );
            // causa interna deve ser SecurityException("Not owner")
            assertNotNull(ex.getCause());
            assertTrue(ex.getCause() instanceof SecurityException);
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }

        // não alterado
        assert(n.getStatus() == NotificationStatus.ACTIVE);
    }

    @Test
    void dismiss_success_changesStatus_whenOwner() throws Exception {
        User principal = new User();
        principal.setId(1);

        Notification n = new Notification();
        n.setId(7L);
        n.setUserId(1L);
        n.setStatus(NotificationStatus.ACTIVE);

        when(repo.findById(7L)).thenReturn(Optional.of(n));

        var auth3 = new UsernamePasswordAuthenticationToken(principal, null);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth3);
        try {
            mockMvc.perform(post("/notifications/7/dismiss"))
                   .andExpect(status().isOk());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }

        assert(n.getStatus() == NotificationStatus.DISMISSED);
        verify(repo).findById(7L);
    }

    @Test
    void countUnread_returnsCount_forUser() throws Exception {
        User principal = new User();
        principal.setId(1);

        when(repo.countByUserIdAndStatus(anyLong(), eq(NotificationStatus.ACTIVE))).thenReturn(42L);
        var auth4 = new UsernamePasswordAuthenticationToken(principal, null);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth4);
        try {
            mockMvc.perform(get("/notifications/unread/count"))
                   .andExpect(status().isOk())
                   .andExpect(content().string("42"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }

        verify(repo).countByUserIdAndStatus(anyLong(), eq(NotificationStatus.ACTIVE));
    }
}
