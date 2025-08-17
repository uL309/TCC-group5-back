package puc.airtrack.airtrack.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
           set n.status = 'EXPIRED', n.expiresAt = :now
         where n.entity = 'OS'
           and n.entityId = :osId
           and n.status = 'ACTIVE'
    """)
    int expireActiveByOs(@Param("osId") String osId, @Param("now") Instant now);

    Optional<Notification> findByEventId(String eventId);

    Page<Notification> findByUserIdAndStatusInOrderByCreatedAtDesc(
        Long userId, Collection<NotificationStatus> statuses, Pageable pageable);

    boolean existsByUserIdAndEntityAndEntityIdAndType(Long userId, String entity, String entityId, NotificationType type);

    boolean existsByUserIdAndEntityAndEntityIdAndTypeAndStatus(Long userId, String entity, String entityId, NotificationType type, NotificationStatus status);
}
