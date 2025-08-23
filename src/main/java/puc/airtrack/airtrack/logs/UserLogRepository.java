package puc.airtrack.airtrack.logs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repositório para logs do módulo User
 */
@Repository
public interface UserLogRepository extends JpaRepository<UserLogEntry, Long> {

    Page<UserLogEntry> findByTargetUserId(String targetUserId, Pageable pageable);
    
    Page<UserLogEntry> findByTimestampBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
    
    Page<UserLogEntry> findByUsername(String username, Pageable pageable);
    
    Page<UserLogEntry> findByOperationType(String operationType, Pageable pageable);
}
