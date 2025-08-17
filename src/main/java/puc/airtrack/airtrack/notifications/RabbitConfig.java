package puc.airtrack.airtrack.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "domain.events";
    public static final String DLX = "domain.dlx";
    public static final String QUEUE = "notifications.q";
    public static final String DLQ = "notifications.dlq";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange dlx() {
        return new TopicExchange(DLX, true, false);
    }

    @Bean
    public Queue dlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(notificationsQueue()).to(exchange()).with("#");
    }

    @Bean
    public Binding dlBinding() {
        return BindingBuilder.bind(dlq()).to(dlx()).with(DLQ);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}

