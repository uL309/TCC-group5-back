package puc.airtrack.airtrack.logs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repositório para logs do módulo Peças
 */
@Repository
public interface PecasLogRepository extends JpaRepository<PecasLogEntry, Long> {

    Page<PecasLogEntry> findByPecaId(String pecaId, Pageable pageable);
    
    Page<PecasLogEntry> findByTimestampBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
    
    Page<PecasLogEntry> findByUsername(String username, Pageable pageable);
    
    Page<PecasLogEntry> findByOperationType(String operationType, Pageable pageable);
}
