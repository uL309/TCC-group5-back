package puc.airtrack.airtrack.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import puc.airtrack.airtrack.Login.User;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository repo;

    @GetMapping
    public Page<Notification> list(@AuthenticationPrincipal User user,
                                   @RequestParam(defaultValue = "ACTIVE") List<NotificationStatus> status,
                                   Pageable pageable) {
        return repo.findByUserIdAndStatusInOrderByCreatedAtDesc(Long.valueOf(user.getId()), status, pageable);
    }

    @PostMapping("/{id}/read")
    @Transactional
    public void markRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        var n = repo.findById(id).orElseThrow();
        if (!n.getUserId().equals((long) user.getId())) throw new SecurityException("Not owner");
        n.setStatus(NotificationStatus.READ);
    }

    @PostMapping("/{id}/dismiss")
    @Transactional
    public void dismiss(@PathVariable Long id, @AuthenticationPrincipal User user) {
        var n = repo.findById(id).orElseThrow();
        if (!n.getUserId().equals((long) user.getId())) throw new SecurityException("Not owner");
        n.setStatus(NotificationStatus.DISMISSED);
    }

    @GetMapping("/unread/count")
    public long countUnread(@AuthenticationPrincipal User user) {
        return repo.countByUserIdAndStatus(Long.valueOf(user.getId()), NotificationStatus.ACTIVE);
    }
}
