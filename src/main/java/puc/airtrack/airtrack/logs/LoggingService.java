package puc.airtrack.airtrack.logs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Serviço central para gerenciamento de logs do sistema
 */
@Service
public class LoggingService {

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
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Obtém o usuário atual a partir do contexto de segurança
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return authentication.getName();
    }
    
    /**
     * Converte um objeto para string JSON
     */
    private String objectToJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "Error converting to JSON: " + e.getMessage();
        }
    }
    
    /**
     * Registra log para o módulo de Cliente
     */
    public ClienteLogEntry logClienteOperation(String controllerMethod, Object request, Object response, 
                                              String clienteId, String operationType) {
        ClienteLogEntry logEntry = new ClienteLogEntry(
            getCurrentUsername(),
            controllerMethod,
            objectToJson(request),
            objectToJson(response),
            clienteId,
            operationType
        );
        return clienteLogRepository.save(logEntry);
    }
    
    /**
     * Registra log para o módulo de Fornecedor
     */
    public FornecedorLogEntry logFornecedorOperation(String controllerMethod, Object request, Object response, 
                                                    String fornecedorId, String operationType) {
        FornecedorLogEntry logEntry = new FornecedorLogEntry(
            getCurrentUsername(),
            controllerMethod,
            objectToJson(request),
            objectToJson(response),
            fornecedorId,
            operationType
        );
        return fornecedorLogRepository.save(logEntry);
    }
    
    /**
     * Registra log para o módulo de Ordem de Serviço
     */
    public OrdemServicoLogEntry logOrdemServicoOperation(String controllerMethod, Object request, Object response, 
                                                        Integer ordemId, String operationType) {
        OrdemServicoLogEntry logEntry = new OrdemServicoLogEntry(
            getCurrentUsername(),
            controllerMethod,
            objectToJson(request),
            objectToJson(response),
            ordemId,
            operationType
        );
        return ordemServicoLogRepository.save(logEntry);
    }
    
    /**
     * Registra log para o módulo de Peças
     */
    public PecasLogEntry logPecasOperation(String controllerMethod, Object request, Object response, 
                                          String pecaId, String operationType) {
        PecasLogEntry logEntry = new PecasLogEntry(
            getCurrentUsername(),
            controllerMethod,
            objectToJson(request),
            objectToJson(response),
            pecaId,
            operationType
        );
        return pecasLogRepository.save(logEntry);
    }
    
    /**
     * Registra log para o módulo de Motor
     */
    public MotorLogEntry logMotorOperation(String controllerMethod, Object request, Object response, 
                                          String motorId, String operationType) {
        MotorLogEntry logEntry = new MotorLogEntry(
            getCurrentUsername(),
            controllerMethod,
            objectToJson(request),
            objectToJson(response),
            motorId,
            operationType
        );
        return motorLogRepository.save(logEntry);
    }
    
    /**
     * Registra log para o módulo de User
     */
    public UserLogEntry logUserOperation(String controllerMethod, Object request, Object response, 
                                        String targetUserId, String operationType) {
        UserLogEntry logEntry = new UserLogEntry(
            getCurrentUsername(),
            controllerMethod,
            objectToJson(request),
            objectToJson(response),
            targetUserId,
            operationType
        );
        return userLogRepository.save(logEntry);
    }
    
    // Métodos para obtenção de logs
    
    public Page<ClienteLogEntry> getClienteLogs(Pageable pageable) {
        return clienteLogRepository.findAll(pageable);
    }
    
    public Page<ClienteLogEntry> getClienteLogsByClienteId(String clienteId, Pageable pageable) {
        return clienteLogRepository.findByClienteId(clienteId, pageable);
    }
    
    public Page<FornecedorLogEntry> getFornecedorLogs(Pageable pageable) {
        return fornecedorLogRepository.findAll(pageable);
    }
    
    public Page<FornecedorLogEntry> getFornecedorLogsByFornecedorId(String fornecedorId, Pageable pageable) {
        return fornecedorLogRepository.findByFornecedorId(fornecedorId, pageable);
    }
    
    public Page<OrdemServicoLogEntry> getOrdemServicoLogs(Pageable pageable) {
        return ordemServicoLogRepository.findAll(pageable);
    }
    
    public Page<OrdemServicoLogEntry> getOrdemServicoLogsByOrdemId(Integer ordemId, Pageable pageable) {
        return ordemServicoLogRepository.findByOrdemId(ordemId, pageable);
    }
    
    public Page<PecasLogEntry> getPecasLogs(Pageable pageable) {
        return pecasLogRepository.findAll(pageable);
    }
    
    public Page<PecasLogEntry> getPecasLogsByPecaId(String pecaId, Pageable pageable) {
        return pecasLogRepository.findByPecaId(pecaId, pageable);
    }
    
    public Page<MotorLogEntry> getMotorLogs(Pageable pageable) {
        return motorLogRepository.findAll(pageable);
    }
    
    public Page<MotorLogEntry> getMotorLogsByMotorId(String motorId, Pageable pageable) {
        return motorLogRepository.findByMotorId(motorId, pageable);
    }
    
    public Page<UserLogEntry> getUserLogs(Pageable pageable) {
        return userLogRepository.findAll(pageable);
    }
    
    public Page<UserLogEntry> getUserLogsByTargetUserId(String targetUserId, Pageable pageable) {
        return userLogRepository.findByTargetUserId(targetUserId, pageable);
    }
}
