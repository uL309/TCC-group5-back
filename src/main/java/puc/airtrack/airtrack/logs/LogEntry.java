package puc.airtrack.airtrack.logs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Classe base para todas as entradas de log
 */
@Data
@NoArgsConstructor
@MappedSuperclass
public abstract class LogEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "controllerMethod")
    private String controllerMethod;
    
    @Column(name = "requestData", columnDefinition = "LONGTEXT")
    private String requestData;
    
    @Column(name = "responseData", columnDefinition = "LONGTEXT")
    private String responseData;
    
    // Constructor for creating new log entries (ID and timestamp will be set by JPA)
    public LogEntry(String username, String controllerMethod, String requestData, String responseData) {
        this.username = username;
        this.controllerMethod = controllerMethod;
        this.requestData = requestData;
        this.responseData = responseData;
    }
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
