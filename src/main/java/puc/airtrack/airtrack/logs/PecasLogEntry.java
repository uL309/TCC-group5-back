package puc.airtrack.airtrack.logs;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade para registro de logs relacionados ao módulo de Peças
 */
@Entity
@Table(name = "logs_pecas")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PecasLogEntry extends LogEntry {
    
    private String pecaId;
    private String operationType;
    
    public PecasLogEntry(String username, String controllerMethod, String requestData, 
                        String responseData, String pecaId, String operationType) {
        super(username, controllerMethod, requestData, responseData);
        this.pecaId = pecaId;
        this.operationType = operationType;
    }
}
