package puc.airtrack.airtrack.logs;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade para registro de logs relacionados ao módulo de Fornecedor
 */
@Entity
@Table(name = "logs_fornecedor")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FornecedorLogEntry extends LogEntry {
    
    private String fornecedorId;
    private String operationType;
    
    public FornecedorLogEntry(String username, String controllerMethod, String requestData, 
                             String responseData, String fornecedorId, String operationType) {
        super(null, null, username, controllerMethod, requestData, responseData);
        this.fornecedorId = fornecedorId;
        this.operationType = operationType;
    }
}
