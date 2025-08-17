package puc.airtrack.airtrack.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.Login.UserRole;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {
    private final NotificationRepository repo;
    private final UserService userService;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    @Transactional
    public void onEvent(DomainEvent e) {
        // Idempotência básica por eventId
        if (e.eventId() != null && repo.findByEventId(e.eventId()).isPresent()) return;

        switch (e.type()) {
            case "OS_CREATED" -> notifyEngineersOnOsCreated(e);
            case "OS_STATUS_CHANGED" -> expireOnOsInProgress(e);
            case "MOTOR_CREATED" -> notifySupervisorsOnMotorCreated(e);
            default -> { /* ignorar ou logar */ }
        }
    }

    private void notifyEngineersOnOsCreated(DomainEvent e) {
        var engineers = userService.findAllByRole(UserRole.ROLE_ENGENHEIRO);
        if (engineers.isEmpty()) return;
        var notifs = engineers.stream().map(user -> {
            var n = new Notification();
            n.setUserId((long) user.getId());
            n.setType("OS_CREATED");
            n.setEntity("OS");
            n.setEntityId(e.entityId());
            n.setTitle("Nova OS disponível");
            n.setBody("Uma ordem de serviço aguarda um engenheiro.");
            n.setEventId(e.eventId());
            return n;
        }).toList();
        repo.saveAll(notifs);
    }

    private void expireOnOsInProgress(DomainEvent e) {
        var newStatus = String.valueOf(e.data().get("new"));
        if ("ANDAMENTO".equalsIgnoreCase(newStatus)) {
            repo.expireActiveByOs(e.entityId(), Instant.now());
        }
    }

    private void notifySupervisorsOnMotorCreated(DomainEvent e) {
        var supers = userService.findAllByRole(UserRole.ROLE_SUPERVISOR);
        if (supers.isEmpty()) return;
        var notifs = supers.stream().map(user -> {
            var n = new Notification();
            n.setUserId((long) user.getId());
            n.setType("MOTOR_CREATED");
            n.setEntity("MOTOR");
            n.setEntityId(e.entityId());
            n.setTitle("Novo motor cadastrado");
            n.setBody("Série: " + e.data().getOrDefault("serie", ""));
            n.setEventId(e.eventId());
            return n;
        }).toList();
        repo.saveAll(notifs);
    }
}
