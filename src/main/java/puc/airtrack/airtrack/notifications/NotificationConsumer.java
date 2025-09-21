package puc.airtrack.airtrack.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.Login.UserRole;
import puc.airtrack.airtrack.OrdemDeServico.OrdemStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
            case OS_PENDING -> notifyEngineersOnOsPending(e);
            case OS_STATUS_CHANGED -> expireOnOsStatusChanged(e);
            case MOTOR_CREATED -> notifySupervisorsOnMotorCreated(e);
            case MOTOR_TBO_EXPIRED -> notifySupervisorsAndAdminsOnMotorTboExpired(e);
            default -> { /* ignorar ou logar */ }
        }
    }

    /**
     * Notifica engenheiros quando uma OS fica pendente.
     */
    private void notifyEngineersOnOsPending(DomainEvent e) {
        var engineers = userService.findAllByRole(UserRole.ROLE_ENGENHEIRO);
        if (engineers.isEmpty()) return;
        List<Notification> notifs = new ArrayList<>();
        String osNumero = e.data().getOrDefault("osNumero", e.entityId()).toString();
        String motorNome = e.data().getOrDefault("motorNome", "").toString();
        for (var user : engineers) {
            boolean exists = repo.existsByUserIdAndEntityAndEntityIdAndTypeAndStatus(
                (long) user.getId(), "OS", e.entityId(), NotificationType.OS_PENDING, NotificationStatus.ACTIVE);
            if (!exists) {
                var n = new Notification();
                n.setUserId((long) user.getId());
                n.setType(NotificationType.OS_PENDING);
                n.setEntity("OS");
                n.setEntityId(e.entityId());
                n.setTitle("OS pendente");
                n.setBody(String.format("A OS <b>#%s</b> do motor <b>%s</b> está <b>pendente</b>.", osNumero, motorNome));
                n.setEventId(e.eventId());
                notifs.add(n);
            }
        }
        if (!notifs.isEmpty()) {
            repo.saveAll(notifs);
        }
    }

    /**
     * Expira notificações ativas de OS quando o status muda para ANDAMENTO ou CONCLUIDA.
     */
    private void expireOnOsStatusChanged(DomainEvent e) {
        Object newStatusObj = e.data().get("new");
        if (newStatusObj == null) return;
        String newStatusStr = newStatusObj.toString();
        OrdemStatus newStatus;
        try {
            newStatus = OrdemStatus.valueOf(newStatusStr);
        } catch (IllegalArgumentException ex) {
            // Status desconhecido, não expira notificações
            return;
        }
        if (newStatus == OrdemStatus.ANDAMENTO || newStatus == OrdemStatus.CONCLUIDA) {
            repo.expireActiveByOs(e.entityId(), Instant.now());
        }
    }

    private void notifySupervisorsOnMotorCreated(DomainEvent e) {
        var supers = userService.findAllByRole(UserRole.ROLE_SUPERVISOR);
        if (supers.isEmpty()) return;
        String serie = e.data().getOrDefault("serie", "").toString();
        var notifs = supers.stream().map(user -> {
            var n = new Notification();
            n.setUserId((long) user.getId());
            n.setType(NotificationType.MOTOR_CREATED);
            n.setEntity("MOTOR");
            n.setEntityId(e.entityId());
            n.setTitle("Novo motor cadastrado");
            n.setBody(String.format("Motor cadastrado com série <b>%s</b>.", serie));
            n.setEventId(e.eventId());
            return n;
        }).toList();
        repo.saveAll(notifs);
    }

    /**
     * Notifica supervisores e admins quando o motor ultrapassa o TBO.
     */
    private void notifySupervisorsAndAdminsOnMotorTboExpired(DomainEvent e) {
        var supervisores = userService.findAllByRole(UserRole.ROLE_SUPERVISOR);
        var admins = userService.findAllByRole(UserRole.ROLE_ADMIN);
        String serie = e.data().getOrDefault("serie", "").toString();
        String marca = e.data().getOrDefault("marca", "").toString();
        String modelo = e.data().getOrDefault("modelo", "").toString();
        String motorId = e.entityId();
        String title = "TBO excedido para motor";
        String body = String.format(
            "O motor de número de série %s, marca %s e modelo %s ultrapassou o limite de horas de operação (TBO). Recomenda-se inspeção e manutenção conforme normas de aviação.",
            serie, marca, modelo
        );
        var notifs = new ArrayList<Notification>();
        for (var user : supervisores) {
            Notification n = new Notification();
            n.setUserId((long) user.getId());
            n.setType(NotificationType.MOTOR_TBO_EXPIRED);
            n.setEntity("MOTOR");
            n.setEntityId(motorId);
            n.setTitle(title);
            n.setBody(body);
            n.setCreatedAt(Instant.now());
            notifs.add(n);
        }
        for (var user : admins) {
            Notification n = new Notification();
            n.setUserId((long) user.getId());
            n.setType(NotificationType.MOTOR_TBO_EXPIRED);
            n.setEntity("MOTOR");
            n.setEntityId(motorId);
            n.setTitle(title);
            n.setBody(body);
            n.setCreatedAt(Instant.now());
            notifs.add(n);
        }
        if (!notifs.isEmpty()) {
            repo.saveAll(notifs);
        }
    }
}
