package puc.airtrack.airtrack.logs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repositório para logs do módulo Fornecedor
 */
@Repository
public interface FornecedorLogRepository extends JpaRepository<FornecedorLogEntry, Long> {

    Page<FornecedorLogEntry> findByFornecedorId(String fornecedorId, Pageable pageable);
    
    Page<FornecedorLogEntry> findByTimestampBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
    
    Page<FornecedorLogEntry> findByUsername(String username, Pageable pageable);
    
    Page<FornecedorLogEntry> findByOperationType(String operationType, Pageable pageable);
}
