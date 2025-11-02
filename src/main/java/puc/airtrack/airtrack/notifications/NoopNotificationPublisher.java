package puc.airtrack.airtrack.notifications;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação Noop do NotificationPublisher
 * Usado quando nenhum publisher (RabbitMQ ou Storage Queue) está disponível
 */
@Service
@Slf4j
@ConditionalOnMissingBean(NotificationPublisher.class)
public class NoopNotificationPublisher implements NotificationPublisher {
    
    public NoopNotificationPublisher() {
        log.warn("⚠️  NotificationPublisher desabilitado - eventos não serão publicados");
    }
    
    @Override
    public void publish(String routingKey, DomainEvent event) {
        log.debug("📭 Evento ignorado (Noop): {} - {}", routingKey, event.eventId());
    }
    
    @Override
    public boolean isEnabled() {
        return false;
    }
}
