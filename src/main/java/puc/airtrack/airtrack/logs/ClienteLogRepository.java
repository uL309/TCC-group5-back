package puc.airtrack.airtrack.logs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repositório para logs do módulo Cliente
 */
@Repository
public interface ClienteLogRepository extends JpaRepository<ClienteLogEntry, Long> {

    Page<ClienteLogEntry> findByClienteId(String clienteId, Pageable pageable);
    
    Page<ClienteLogEntry> findByTimestampBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
    
    Page<ClienteLogEntry> findByUsername(String username, Pageable pageable);
    
    Page<ClienteLogEntry> findByOperationType(String operationType, Pageable pageable);
}
