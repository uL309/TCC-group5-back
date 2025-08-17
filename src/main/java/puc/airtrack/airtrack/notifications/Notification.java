package puc.airtrack.airtrack.notifications;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "notificacao",
    indexes = @Index(name = "idx_user_status_created", columnList = "user_id,status,created_at")
)
public class Notification {
    // Getters and setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;   // ex.: OS_PENDING, OS_STATUS_CHANGED, MOTOR_CREATED

    @Column(nullable = false, length = 30)
    private String entity; // ex.: OS, MOTOR

    @Column(name = "entity_id", nullable = false, length = 64)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.ACTIVE;

    @Column(length = 140)
    private String title;

    @Column(length = 1000)
    private String body;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "event_id", length = 50)
    private String eventId;

}
