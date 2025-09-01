package puc.airtrack.airtrack.logs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller para acesso aos logs do sistema
 */
@RestController
@RequestMapping("/api/logs")
public class LogsController {

    @Autowired
    private LoggingService loggingService;
    
    @Autowired
    private ClienteLogRepository clienteLogRepository;
    
    @Autowired
    private FornecedorLogRepository fornecedorLogRepository;
    
    @Autowired
    private OrdemServicoLogRepository ordemServicoLogRepository;
    
    @Autowired
    private PecasLogRepository pecasLogRepository;
    
    @Autowired
    private MotorLogRepository motorLogRepository;
    
    @Autowired
    private UserLogRepository userLogRepository;
    
    /**
     * Obter logs do módulo Cliente
     */
    @GetMapping("/clientes")
    public ResponseEntity<Page<ClienteLogEntry>> getClienteLogs(
            @RequestParam(required = false) String clienteId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        
        Page<ClienteLogEntry> logs;
        
        if (clienteId != null) {
            logs = clienteLogRepository.findByClienteId(clienteId, pageable);
        } else if (username != null) {
            logs = clienteLogRepository.findByUsername(username, pageable);
        } else if (operationType != null) {
            logs = clienteLogRepository.findByOperationType(operationType, pageable);
        } else if (startDate != null && endDate != null) {
            logs = clienteLogRepository.findByTimestampBetween(startDate, endDate, pageable);
        } else {
            logs = clienteLogRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(logs);
    }

    /**
     * Obter logs do módulo Fornecedor
     */
    @GetMapping("/fornecedores")
    public ResponseEntity<Page<FornecedorLogEntry>> getFornecedorLogs(
            @RequestParam(required = false) String fornecedorId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        
        Page<FornecedorLogEntry> logs;
        
        if (fornecedorId != null) {
            logs = fornecedorLogRepository.findByFornecedorId(fornecedorId, pageable);
        } else if (username != null) {
            logs = fornecedorLogRepository.findByUsername(username, pageable);
        } else if (operationType != null) {
            logs = fornecedorLogRepository.findByOperationType(operationType, pageable);
        } else if (startDate != null && endDate != null) {
            logs = fornecedorLogRepository.findByTimestampBetween(startDate, endDate, pageable);
        } else {
            logs = fornecedorLogRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(logs);
    }

    /**
     * Obter logs do módulo Ordem de Serviço
     */
    @GetMapping("/ordens")
    public ResponseEntity<Page<OrdemServicoLogEntry>> getOrdemServicoLogs(
            @RequestParam(required = false) Integer ordemId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        
        Page<OrdemServicoLogEntry> logs;
        
        if (ordemId != null) {
            logs = ordemServicoLogRepository.findByOrdemId(ordemId, pageable);
        } else if (username != null) {
            logs = ordemServicoLogRepository.findByUsername(username, pageable);
        } else if (operationType != null) {
            logs = ordemServicoLogRepository.findByOperationType(operationType, pageable);
        } else if (startDate != null && endDate != null) {
            logs = ordemServicoLogRepository.findByTimestampBetween(startDate, endDate, pageable);
        } else {
            logs = ordemServicoLogRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(logs);
    }

    /**
     * Obter logs do módulo Peças
     */
    @GetMapping("/pecas")
    public ResponseEntity<Page<PecasLogEntry>> getPecasLogs(
            @RequestParam(required = false) String pecaId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        
        Page<PecasLogEntry> logs;
        
        if (pecaId != null) {
            logs = pecasLogRepository.findByPecaId(pecaId, pageable);
        } else if (username != null) {
            logs = pecasLogRepository.findByUsername(username, pageable);
        } else if (operationType != null) {
            logs = pecasLogRepository.findByOperationType(operationType, pageable);
        } else if (startDate != null && endDate != null) {
            logs = pecasLogRepository.findByTimestampBetween(startDate, endDate, pageable);
        } else {
            logs = pecasLogRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(logs);
    }

    /**
     * Obter logs do módulo Motor
     */
    @GetMapping("/motores")
    public ResponseEntity<Page<MotorLogEntry>> getMotorLogs(
            @RequestParam(required = false) String motorId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        
        Page<MotorLogEntry> logs;
        
        if (motorId != null) {
            logs = motorLogRepository.findByMotorId(motorId, pageable);
        } else if (username != null) {
            logs = motorLogRepository.findByUsername(username, pageable);
        } else if (operationType != null) {
            logs = motorLogRepository.findByOperationType(operationType, pageable);
        } else if (startDate != null && endDate != null) {
            logs = motorLogRepository.findByTimestampBetween(startDate, endDate, pageable);
        } else {
            logs = motorLogRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(logs);
    }

    /**
     * Obter logs do módulo User
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserLogEntry>> getUserLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        
        Page<UserLogEntry> logs;
        
        if (userId != null) {
            logs = userLogRepository.findByTargetUserId(userId, pageable);
        } else if (username != null) {
            logs = userLogRepository.findByUsername(username, pageable);
        } else if (operationType != null) {
            logs = userLogRepository.findByOperationType(operationType, pageable);
        } else if (startDate != null && endDate != null) {
            logs = userLogRepository.findByTimestampBetween(startDate, endDate, pageable);
        } else {
            logs = userLogRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(logs);
    }
}
