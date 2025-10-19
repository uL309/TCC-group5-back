package puc.airtrack.airtrack.logs;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade para registro de logs relacionados ao módulo de Cliente
 */
@Entity
@Table(name = "logs_cliente")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ClienteLogEntry extends LogEntry {
    
    private String clienteId;
    private String operationType;
    
    public ClienteLogEntry(String username, String controllerMethod, String requestData, 
                          String responseData, String clienteId, String operationType) {
        super(username, controllerMethod, requestData, responseData);
        this.clienteId = clienteId;
        this.operationType = operationType;
    }
}
