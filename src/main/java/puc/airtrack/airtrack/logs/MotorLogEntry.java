package puc.airtrack.airtrack.logs;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade para registro de logs relacionados ao módulo de Motor
 */
@Entity
@Table(name = "logs_motor")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MotorLogEntry extends LogEntry {
    
    private String motorId;
    private String operationType;
    
    public MotorLogEntry(String username, String controllerMethod, String requestData, 
                        String responseData, String motorId, String operationType) {
        super(username, controllerMethod, requestData, responseData);
        this.motorId = motorId;
        this.operationType = operationType;
    }
}
