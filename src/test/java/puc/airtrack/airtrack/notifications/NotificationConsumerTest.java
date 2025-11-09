package puc.airtrack.airtrack.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserRole;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.notifications.DomainEvent;
import puc.airtrack.airtrack.notifications.Notification;
import puc.airtrack.airtrack.notifications.NotificationConsumer;
import puc.airtrack.airtrack.notifications.NotificationRepository;
import puc.airtrack.airtrack.notifications.NotificationStatus;
import puc.airtrack.airtrack.notifications.NotificationType;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserService userService;

    @InjectMocks
    private NotificationConsumer consumer;

    private static List<Notification> toList(Iterable<Notification> it) {
        java.util.ArrayList<Notification> list = new java.util.ArrayList<>();
        if (it != null) it.forEach(list::add);
        return list;
    }

    private User user(int id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        u.setName(role.name() + "-" + id);
        return u;
    }

    // DomainEvent(eventId, type, entity, entityId, actorId, timestamp, data)
    private DomainEvent evt(String id, NotificationType type, String entityId, Map<String,Object> data) {
        String entity = switch (type) {
            case OS_PENDING, OS_STATUS_CHANGED -> "OS";
            case MOTOR_CREATED, MOTOR_TBO_EXPIRED, MOTOR_TBO_EXPIRED_CLEAR -> "MOTOR";
        };
        return new DomainEvent(id, type, entity, entityId, null, Instant.now(), data);
    }

    @Test
    void idempotencia_naoProcessaEventoJaExistente() {
        when(notificationRepository.findByEventId("IDEMP"))
                .thenReturn(Optional.of(new Notification()));
        consumer.onEvent(evt("IDEMP", NotificationType.OS_PENDING, "OS10", Map.of()));
        verify(notificationRepository, never()).saveAll(anyList());
    }

    @Test
    void osPending_criaNotificacaoParaCadaEngenheiroAusente() {
        User e1 = user(1, UserRole.ROLE_ENGENHEIRO);
        User e2 = user(2, UserRole.ROLE_ENGENHEIRO);
        when(notificationRepository.findByEventId("P1")).thenReturn(Optional.empty());
        when(userService.findAllByRole(UserRole.ROLE_ENGENHEIRO)).thenReturn(List.of(e1, e2));
        when(notificationRepository.existsByUserIdAndEntityAndEntityIdAndTypeAndStatus(anyLong(), anyString(), anyString(), any(), any()))
                .thenReturn(false);

        consumer.onEvent(evt("P1", NotificationType.OS_PENDING, "OS20", Map.of("motorNome","MTR-X", "osNumero","20")));

        ArgumentCaptor<Iterable<Notification>> cap = ArgumentCaptor.forClass(Iterable.class);
        verify(notificationRepository).saveAll(cap.capture());
        List<Notification> saved = toList(cap.getValue());
        assertEquals(2, saved.size());
        assertTrue(saved.stream().allMatch(n -> n.getType() == NotificationType.OS_PENDING));
    }

    @Test
    void osPending_naoDuplicaSeExiste() {
        User e1 = user(1, UserRole.ROLE_ENGENHEIRO);
        when(notificationRepository.findByEventId("P2")).thenReturn(Optional.empty());
        when(userService.findAllByRole(UserRole.ROLE_ENGENHEIRO)).thenReturn(List.of(e1));
        when(notificationRepository.existsByUserIdAndEntityAndEntityIdAndTypeAndStatus(anyLong(), anyString(), anyString(), any(), any()))
                .thenReturn(true);

        consumer.onEvent(evt("P2", NotificationType.OS_PENDING, "OS21", Map.of()));
        verify(notificationRepository, never()).saveAll(anyList());
    }

    @Test
    void osStatusChanged_expiraQuandoAndamentoOuConcluida() {
        when(notificationRepository.findByEventId("SC1")).thenReturn(Optional.empty());
        consumer.onEvent(evt("SC1", NotificationType.OS_STATUS_CHANGED, "OS30", Map.of("new","ANDAMENTO")));
        verify(notificationRepository).expireActiveByOs(eq("OS30"), any(Instant.class));

        reset(notificationRepository);
        when(notificationRepository.findByEventId("SC2")).thenReturn(Optional.empty());
        consumer.onEvent(evt("SC2", NotificationType.OS_STATUS_CHANGED, "OS31", Map.of("new","CONCLUIDA")));
        verify(notificationRepository).expireActiveByOs(eq("OS31"), any(Instant.class));
    }

    @Test
    void osStatusChanged_outroStatusNaoExpira() {
        when(notificationRepository.findByEventId("SC3")).thenReturn(Optional.empty());
        consumer.onEvent(evt("SC3", NotificationType.OS_STATUS_CHANGED, "OS32", Map.of("new","PENDENTE")));
        verify(notificationRepository, never()).expireActiveByOs(anyString(), any());
    }

    @Test
    void motorCreated_notificaSupervisores() {
        User s1 = user(10, UserRole.ROLE_SUPERVISOR);
        when(notificationRepository.findByEventId("MC1")).thenReturn(Optional.empty());
        when(userService.findAllByRole(UserRole.ROLE_SUPERVISOR)).thenReturn(List.of(s1));

        consumer.onEvent(evt("MC1", NotificationType.MOTOR_CREATED, "MOT10", Map.of("serie","S-10")));

        ArgumentCaptor<Iterable<Notification>> cap = ArgumentCaptor.forClass(Iterable.class);
        verify(notificationRepository).saveAll(cap.capture());
        List<Notification> saved = toList(cap.getValue());
        assertEquals(1, saved.size());
        assertEquals(NotificationType.MOTOR_CREATED, saved.get(0).getType());
    }

    @Test
    void motorTboExpired_notificaSupervisoresEAdmins() {
        User sup = user(1, UserRole.ROLE_SUPERVISOR);
        User adm = user(2, UserRole.ROLE_ADMIN);
        when(notificationRepository.findByEventId("TB1")).thenReturn(Optional.empty());
        when(userService.findAllByRole(UserRole.ROLE_SUPERVISOR)).thenReturn(List.of(sup));
        when(userService.findAllByRole(UserRole.ROLE_ADMIN)).thenReturn(List.of(adm));

        consumer.onEvent(evt("TB1", NotificationType.MOTOR_TBO_EXPIRED, "MOT99", Map.of("serie","S-99", "marca","X", "modelo","Y")));

        ArgumentCaptor<Iterable<Notification>> cap = ArgumentCaptor.forClass(Iterable.class);
        verify(notificationRepository).saveAll(cap.capture());
        List<Notification> saved = toList(cap.getValue());
        assertEquals(2, saved.size());
        assertTrue(saved.stream().allMatch(n -> n.getType() == NotificationType.MOTOR_TBO_EXPIRED));
    }

    @Test
    void motorTboExpiredClear_expira() {
        when(notificationRepository.findByEventId("CL1")).thenReturn(Optional.empty());
        consumer.onEvent(evt("CL1", NotificationType.MOTOR_TBO_EXPIRED_CLEAR, "MOT88", Map.of()));
        verify(notificationRepository).updateStatusByEntityAndEntityIdAndType(
                eq("MOTOR"), eq("MOT88"), eq(NotificationType.MOTOR_TBO_EXPIRED),
                eq(NotificationStatus.EXPIRED), any(Instant.class));
    }
}