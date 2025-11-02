package puc.airtrack.airtrack.notifications;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * Implementação RabbitMQ do NotificationPublisher
 * Usado para desenvolvimento local
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class DomainEventPublisher implements NotificationPublisher {
    private final RabbitTemplate rabbit;

    @Override
    public void publish(String routingKey, DomainEvent event) {
        rabbit.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event, m -> {
            m.getMessageProperties().setMessageId(event.eventId());
            m.getMessageProperties().setType(String.valueOf(event.type()));
            return m;
        });
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}