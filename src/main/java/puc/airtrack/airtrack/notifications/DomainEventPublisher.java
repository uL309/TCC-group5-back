package puc.airtrack.airtrack.notifications;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DomainEventPublisher {
    private final RabbitTemplate rabbit;

    public void publish(String routingKey, DomainEvent event) {
        rabbit.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event, m -> {
            m.getMessageProperties().setMessageId(event.eventId());
            m.getMessageProperties().setType(String.valueOf(event.type()));
            return m;
        });
    }
}

