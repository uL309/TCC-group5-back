package puc.airtrack.airtrack.logs;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade para registro de logs relacionados ao módulo de Usuário
 */
@Entity
@Table(name = "logs_user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserLogEntry extends LogEntry {
    
    private String targetUserId;
    private String operationType;
    
    public UserLogEntry(String username, String controllerMethod, String requestData, 
                       String responseData, String targetUserId, String operationType) {
        super(null, null, username, controllerMethod, requestData, responseData);
        this.targetUserId = targetUserId;
        this.operationType = operationType;
    }
}
