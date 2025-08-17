package puc.airtrack.airtrack.notifications;

import java.time.Instant;
import java.util.Map;

public record DomainEvent(
    String eventId,
    NotificationType type,
    String entity,
    String entityId,
    String actorId,
    Instant timestamp,
    Map<String, Object> data
) {}

