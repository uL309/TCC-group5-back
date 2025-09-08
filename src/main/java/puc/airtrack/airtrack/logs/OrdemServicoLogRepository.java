package puc.airtrack.airtrack.logs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repositório para logs do módulo Ordem de Serviço
 */
@Repository
public interface OrdemServicoLogRepository extends JpaRepository<OrdemServicoLogEntry, Long> {

    Page<OrdemServicoLogEntry> findByOrdemId(Integer ordemId, Pageable pageable);
    
    Page<OrdemServicoLogEntry> findByTimestampBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
    
    Page<OrdemServicoLogEntry> findByUsername(String username, Pageable pageable);
    
    Page<OrdemServicoLogEntry> findByOperationType(String operationType, Pageable pageable);
}
