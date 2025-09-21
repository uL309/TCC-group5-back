package puc.airtrack.airtrack.OrdemDeServico;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import puc.airtrack.airtrack.Cliente.ClienteRepo;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserRole;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.Motor.Motor;
import puc.airtrack.airtrack.Motor.MotorRepository;
import puc.airtrack.airtrack.notifications.DomainEvent;
import puc.airtrack.airtrack.notifications.DomainEventPublisher;
import puc.airtrack.airtrack.notifications.NotificationType;
import puc.airtrack.airtrack.services.AuthUtil;
import puc.airtrack.airtrack.tipoMotor.TipoMotor;
import puc.airtrack.airtrack.tipoMotor.TipoMotorRepository;

@Service
public class CabecalhoOrdemService {

    @Autowired CabecalhoOrdemRepository cabecalhoOrdemRepository;
    @Autowired
    ClienteRepo clienteRepo;
    @Autowired
    MotorRepository motorRepository;
    @Autowired
    UserService userService;
    @Autowired
    LinhaOrdemService linhaOrdemService;
    @Autowired
    DomainEventPublisher domainEventPublisher;
    @Autowired
    TipoMotorRepository tipoMotorRepository;

    public ResponseEntity<String> createCabecalho(CabecalhoOrdemDTO dto){
        if (dto != null) {
            CabecalhoOrdem entity = new CabecalhoOrdem();
            entity.setDataAbertura(dto.getDataAbertura());
            entity.setDataFechamento(dto.getDataFechamento());
            entity.setDescricao(dto.getDescricao());
            entity.setTipo(dto.getTipo());
            entity.setTempoUsado(dto.getTempoUsado());
            entity.setTempoEstimado((int) dto.getTempoEstimado());
            OrdemStatus status = OrdemStatus.values()[dto.getStatus()];
            entity.setStatus(obterStatusCabecalho(Boolean.TRUE, status));
            entity.setValorHora(dto.getValorHora());
            if (dto.getClienteId() != null) {
                entity.setCliente(clienteRepo.findByCpf(dto.getClienteId()).orElse(null));
            }
            if (dto.getMotorId() != null) {
                try {
                    int motorId = Integer.parseInt(dto.getMotorId());
                    entity.setNumSerieMotor(motorRepository.findById(motorId).orElse(null));
                } catch (NumberFormatException e) {
                    entity.setNumSerieMotor(null);
                }
            }
            if (dto.getSupervisorId() != null) {
                try {
                    int supervisorId = Integer.parseInt(dto.getSupervisorId());
                    entity.setSupervisor(userService.findById(supervisorId));
                } catch (NumberFormatException e) {
                    entity.setSupervisor(null);
                }
            }
            cabecalhoOrdemRepository.save(entity);
            for (LinhaOrdemDTO linha : dto.getLinhas()) {
                linha.setOrdemId(entity.getId());
                linhaOrdemService.create(linha);
            }
            // Atualiza o status do cabeçalho após criar as linhas, se necessário
            entity.setStatus(obterStatusCabecalho(Boolean.FALSE, entity.getStatus()));
            cabecalhoOrdemRepository.save(entity);
            URI location = URI.create("/ordem/get?id=" + entity.getId());

            // Publica evento OS_PENDING se status for PENDENTE
            if (entity.getStatus() == OrdemStatus.PENDENTE) {
                publishPendingEvent(entity);
            }
            return ResponseEntity.created(location).body("CabecalhoOrdem created successfully");
        }
        return ResponseEntity.badRequest().body("Invalid data");
    }

    @Transactional
    public ResponseEntity<String> updateCabecalho(CabecalhoOrdemDTO dto) {
        if (dto != null && dto.getId() != null) {
            Optional<CabecalhoOrdem> opt = cabecalhoOrdemRepository.findById(dto.getId());
            if (opt.isPresent()) {
                CabecalhoOrdem entity = opt.get();
                OrdemStatus oldStatus = entity.getStatus();

                entity.setDataAbertura(dto.getDataAbertura());
                entity.setDataFechamento(dto.getDataFechamento());
                entity.setDescricao(dto.getDescricao());
                entity.setTipo(dto.getTipo());
                entity.setTempoUsado(dto.getTempoUsado());
                entity.setTempoEstimado((int) dto.getTempoEstimado());
                OrdemStatus status = OrdemStatus.values()[dto.getStatus()];
                entity.setStatus(obterStatusCabecalho(Boolean.FALSE, status));
                entity.setTempoUsado(dto.getTempoUsado());
                entity.setValorHora(dto.getValorHora());

                if (dto.getClienteId() != null) {
                    entity.setCliente(clienteRepo.findByCpf(dto.getClienteId()).orElse(null));
                }

                if (dto.getMotorId() != null) {
                    try {
                        int motorId = Integer.parseInt(dto.getMotorId());
                        Motor motor = motorRepository.findById(motorId).orElse(null);
                        if (motor != null) {
                            TipoMotor tipoMotor = tipoMotorRepository.findByMarcaAndModelo(motor.getMarca(), motor.getModelo());
                            if (tipoMotor != null) {
                                motor.setHoras_operacao(dto.getHorasOperacaoMotor());
                                motorRepository.save(motor);
                                if (dto.getHorasOperacaoMotor() > tipoMotor.getTbo()) {
                                    // Publica evento para notificação de TBO excedido
                                    publishMotorTboExpiredEvent(motor);
                                } else {
                                    domainEventPublisher.publish(
                                        "motor.tbo.expired.clear",
                                        new DomainEvent(
                                            UUID.randomUUID().toString(),
                                            NotificationType.MOTOR_TBO_EXPIRED_CLEAR,
                                            "MOTOR",
                                            String.valueOf(motor.getId()),
                                            null,
                                            Instant.now(),
                                            new HashMap<>()
                                        )
                                    );
                                }
                            }
                            entity.setNumSerieMotor(motor);
                        }




                    } catch (NumberFormatException e) {
                        entity.setNumSerieMotor(null);
                    }
                }

                if (dto.getSupervisorId() != null) {
                    try {
                        int supervisorId = Integer.parseInt(dto.getSupervisorId());
                        entity.setSupervisor(userService.findById(supervisorId));
                    } catch (NumberFormatException e) {
                        entity.setSupervisor(null);
                    }
                }

                User usuario = AuthUtil.getUsuarioLogado();
                UserRole userRole = usuario != null ? usuario.getRole() : null;

                if (userRole == UserRole.ROLE_ENGENHEIRO) {
                    entity.setEngenheiroAtuante(usuario);
                }

                linhaOrdemService.deleteAllByOrdemId(entity.getId());
                cabecalhoOrdemRepository.save(entity);

                for (LinhaOrdemDTO linha : dto.getLinhas()) {
                    linha.setOrdemId(entity.getId()); // garante vinculação
                    linhaOrdemService.create(linha);
                }

                // Publica evento se mudou para EM_ANDAMENTO
                OrdemStatus newStatus = entity.getStatus();
                if (oldStatus != OrdemStatus.PENDENTE && newStatus == OrdemStatus.PENDENTE) {
                    publishPendingEvent(entity);
                } else {
                    publishStatusChangedEvent(entity, oldStatus, newStatus);
                }
                return ResponseEntity.ok("CabecalhoOrdem updated successfully");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CabecalhoOrdem not found");
    }

    @Transactional
    public ResponseEntity<String> atualizarStatusCabecalho(int cabecalhoId, int novoStatus) {
        Optional<CabecalhoOrdem> opt = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (opt.isPresent()) {
            CabecalhoOrdem entity = opt.get();
            OrdemStatus oldStatus = entity.getStatus();
            OrdemStatus status = OrdemStatus.values()[novoStatus];
            entity.setStatus(status);
            cabecalhoOrdemRepository.save(entity);
            OrdemStatus newStatus = entity.getStatus();
            if (oldStatus != OrdemStatus.PENDENTE && newStatus == OrdemStatus.PENDENTE) {
                publishPendingEvent(entity);
            } else {
                publishStatusChangedEvent(entity, oldStatus, newStatus);
            }
            return ResponseEntity.ok("Status atualizado com sucesso");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cabeçalho não encontrado");
    }

    /**
     * Publica um evento OS_PENDING no RabbitMQ quando o status da OS é PENDENTE.
     */
    private void publishPendingEvent(CabecalhoOrdem entity) {
        String eventId = UUID.randomUUID().toString();
        User usuario = AuthUtil.getUsuarioLogado();
        String actorId = usuario != null ? String.valueOf(usuario.getId()) : null;
        HashMap<String, Object> data = new HashMap<>();
        data.put("osNumero", entity.getId());
        String motorSerie = entity.getNumSerieMotor() != null ? entity.getNumSerieMotor().getSerie_motor() : "";
        data.put("motorNome", motorSerie);
        try {
            domainEventPublisher.publish(
                "os.pending",
                new DomainEvent(
                    eventId,
                    NotificationType.OS_PENDING,
                    "OS",
                    String.valueOf(entity.getId()),
                    actorId,
                    Instant.now(),
                    data
                )
            );
        } catch (Exception ex) {
            // Apenas loga e ignora se RabbitMQ estiver fora do ar
            System.err.println("[WARN] Falha ao publicar evento de notificação: " + ex.getMessage());
        }
    }

    /**
     * Publica um evento OS_STATUS_CHANGED no RabbitMQ quando o status da OS muda para ANDAMENTO ou CONCLUIDA.
     */
    private void publishStatusChangedEvent(CabecalhoOrdem entity, OrdemStatus oldStatus, OrdemStatus newStatus) {
        if ((oldStatus != OrdemStatus.ANDAMENTO && newStatus == OrdemStatus.ANDAMENTO) ||
            (oldStatus != OrdemStatus.CONCLUIDA && newStatus == OrdemStatus.CONCLUIDA)) {
            String eventId = UUID.randomUUID().toString();
            User usuario = AuthUtil.getUsuarioLogado();
            String actorId = usuario != null ? String.valueOf(usuario.getId()) : null;
            HashMap<String, Object> data = new HashMap<>();
            data.put("old", oldStatus.name());
            data.put("new", newStatus.name());
            try {
                domainEventPublisher.publish(
                    "os.status.changed",
                    new DomainEvent(
                        eventId,
                        NotificationType.OS_STATUS_CHANGED,
                        "OS",
                        String.valueOf(entity.getId()),
                        actorId,
                        Instant.now(),
                        data
                    )
                );
            } catch (Exception ex) {
                // Apenas loga e ignora se RabbitMQ estiver fora do ar
                System.err.println("[WARN] Falha ao publicar evento de notificação: " + ex.getMessage());
            }
        }
    }

    private void publishMotorTboExpiredEvent(Motor motor) {
        String eventId = UUID.randomUUID().toString();
        HashMap<String, Object> data = new HashMap<>();
        data.put("serie", motor.getSerie_motor());
        data.put("marca", motor.getMarca());
        data.put("modelo", motor.getModelo());
        domainEventPublisher.publish(
            "motor.tbo.expired",
            new DomainEvent(
                eventId,
                NotificationType.MOTOR_TBO_EXPIRED,
                "MOTOR",
                String.valueOf(motor.getId()),
                null,
                Instant.now(),
                data
            )
        );
    }


    public OrdemStatus obterStatusCabecalho(boolean isNovoOs, OrdemStatus statusAtual) {
        User usuario = AuthUtil.getUsuarioLogado();
        UserRole userRole = usuario != null ? usuario.getRole() : null;

        if (isNovoOs && userRole == UserRole.ROLE_SUPERVISOR) {
            return OrdemStatus.PENDENTE;
        }
        if (userRole == UserRole.ROLE_ENGENHEIRO && statusAtual == OrdemStatus.PENDENTE) {
            return OrdemStatus.ANDAMENTO;
        }
        return statusAtual;
    }
}

