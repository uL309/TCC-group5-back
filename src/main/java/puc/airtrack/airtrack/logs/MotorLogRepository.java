package puc.airtrack.airtrack.logs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repositório para logs do módulo Motor
 */
@Repository
public interface MotorLogRepository extends JpaRepository<MotorLogEntry, Long> {

    Page<MotorLogEntry> findByMotorId(String motorId, Pageable pageable);
    
    Page<MotorLogEntry> findByTimestampBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
    
    Page<MotorLogEntry> findByUsername(String username, Pageable pageable);
    
    Page<MotorLogEntry> findByOperationType(String operationType, Pageable pageable);
}
