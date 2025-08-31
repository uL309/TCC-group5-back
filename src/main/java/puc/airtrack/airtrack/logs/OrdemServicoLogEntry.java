package puc.airtrack.airtrack.logs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade para registro de logs relacionados ao módulo de Ordem de Serviço
 */
@Entity
@Table(name = "logs_ordem_servico")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrdemServicoLogEntry extends LogEntry {
    
    private Integer ordemId;
    private String operationType;
    
    // Use LONGTEXT for MySQL to handle large request data
    @Column(columnDefinition = "LONGTEXT")
    private String requestData;
    
    // Use LONGTEXT for MySQL to handle large response data
    @Column(columnDefinition = "LONGTEXT")
    private String responseData;
    
    public OrdemServicoLogEntry(String username, String controllerMethod, String requestData, 
                               String responseData, Integer ordemId, String operationType) {
        super(null, null, username, controllerMethod, requestData, responseData);
        this.ordemId = ordemId;
        this.operationType = operationType;
    }
}
