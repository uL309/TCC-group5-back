package puc.airtrack.airtrack.notifications;

/**
 * Interface para publicação de eventos de notificação.
 * Permite trocar entre RabbitMQ (local) e Storage Queues (Azure).
 */
public interface NotificationPublisher {
    
    /**
     * Publica um evento de domínio
     * @param routingKey Chave de roteamento (topic para RabbitMQ, metadata para Storage Queue)
     * @param event Evento a ser publicado
     */
    void publish(String routingKey, DomainEvent event);
    
    /**
     * Indica se o publisher está disponível/habilitado
     */
    boolean isEnabled();
}
