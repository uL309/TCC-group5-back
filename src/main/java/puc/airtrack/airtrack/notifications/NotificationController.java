package puc.airtrack.airtrack.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import puc.airtrack.airtrack.Login.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Gerenciamento de notificações do sistema - TBO próximo, OS pendentes, alertas")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    private final NotificationRepository repo;

    @Operation(
        summary = "Listar notificações",
        description = "Lista todas as notificações do usuário autenticado, com opção de filtrar por status (ACTIVE, READ, DISMISSED)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificações retornadas com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @GetMapping
    public Page<Notification> list(@AuthenticationPrincipal User user,
                                   @RequestParam(defaultValue = "ACTIVE") List<NotificationStatus> status,
                                   Pageable pageable) {
        return repo.findByUserIdAndStatusInOrderByCreatedAtDesc(Long.valueOf(user.getId()), status, pageable);
    }

    @Operation(
        summary = "Marcar notificação como lida",
        description = "Marca uma notificação específica como lida pelo usuário autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificação marcada como lida"),
        @ApiResponse(responseCode = "403", description = "Notificação pertence a outro usuário"),
        @ApiResponse(responseCode = "404", description = "Notificação não encontrada")
    })
    @PostMapping("/{id}/read")
    @Transactional
    public void markRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        var n = repo.findById(id).orElseThrow();
        if (!n.getUserId().equals((long) user.getId())) throw new SecurityException("Not owner");
        n.setStatus(NotificationStatus.READ);
    }

    @Operation(
        summary = "Dispensar notificação",
        description = "Marca uma notificação como dispensada (dismissed), removendo-a da visualização principal."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificação dispensada"),
        @ApiResponse(responseCode = "403", description = "Notificação pertence a outro usuário"),
        @ApiResponse(responseCode = "404", description = "Notificação não encontrada")
    })
    @PostMapping("/{id}/dismiss")
    @Transactional
    public void dismiss(@PathVariable Long id, @AuthenticationPrincipal User user) {
        var n = repo.findById(id).orElseThrow();
        if (!n.getUserId().equals((long) user.getId())) throw new SecurityException("Not owner");
        n.setStatus(NotificationStatus.DISMISSED);
    }

    @Operation(
        summary = "Contar notificações não lidas",
        description = "Retorna a quantidade de notificações ativas (não lidas) do usuário autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contagem retornada"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @GetMapping("/unread/count")
    public long countUnread(@AuthenticationPrincipal User user) {
        return repo.countByUserIdAndStatus(Long.valueOf(user.getId()), NotificationStatus.ACTIVE);
    }
}
