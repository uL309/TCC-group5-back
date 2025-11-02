package puc.airtrack.airtrack.notifications;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação Azure Storage Queue do NotificationPublisher
 * Usado para produção no Azure
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "azure.storage.queue.enabled", havingValue = "true", matchIfMissing = false)
public class StorageQueuePublisher implements NotificationPublisher {
    
    private final QueueClient queueClient;
    private final ObjectMapper objectMapper;
    
    public StorageQueuePublisher(
            @Value("${azure.storage.queue.connection-string}") String connectionString,
            @Value("${azure.storage.queue.queue-name:airtrack-notifications}") String queueName,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.queueClient = new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .buildClient();
        
        // Cria a fila se não existir
        try {
            queueClient.createIfNotExists();
            log.info("Storage Queue '{}' configurada com sucesso", queueName);
        } catch (Exception e) {
            log.error("Erro ao criar Storage Queue: {}", e.getMessage());
        }
    }
    
    @Override
    public void publish(String routingKey, DomainEvent event) {
        try {
            // Serializa o evento para JSON
            String messageJson = objectMapper.writeValueAsString(event);
            
            // Adiciona metadata do routingKey como propriedade
            queueClient.sendMessage(messageJson);
            
            log.debug("Evento publicado na Storage Queue: {} - {}", routingKey, event.eventId());
        } catch (Exception e) {
            log.error("Erro ao publicar evento na Storage Queue: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
