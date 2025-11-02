package puc.airtrack.airtrack.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Health Check", description = "Endpoint de verificação de saúde da aplicação")
public class HealthCheckController {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    private final Instant startTime = Instant.now();

    @Operation(
        summary = "Health Check",
        description = "Verifica se a aplicação está funcionando corretamente. Usado pelo Azure Container Apps para health probes."
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());
        health.put("uptime", java.time.Duration.between(startTime, Instant.now()).getSeconds() + "s");
        
        // Informações da aplicação
        if (buildProperties != null) {
            health.put("version", buildProperties.getVersion());
            health.put("name", buildProperties.getName());
        } else {
            health.put("version", "0.0.1-SNAPSHOT");
            health.put("name", "airtrack");
        }

        // Verifica conexão com banco de dados
        try {
            dataSource.getConnection().close();
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("database_error", e.getMessage());
        }

        // Profile ativo
        String profile = System.getProperty("spring.profiles.active", "default");
        health.put("profile", profile);

        return ResponseEntity.ok(health);
    }

    @Operation(
        summary = "Readiness Probe",
        description = "Verifica se a aplicação está pronta para receber tráfego"
    )
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        Map<String, String> readiness = new HashMap<>();
        
        // Verifica se o banco de dados está acessível
        try {
            dataSource.getConnection().close();
            readiness.put("status", "ready");
            return ResponseEntity.ok(readiness);
        } catch (Exception e) {
            readiness.put("status", "not ready");
            readiness.put("reason", "database unavailable");
            return ResponseEntity.status(503).body(readiness);
        }
    }

    @Operation(
        summary = "Liveness Probe",
        description = "Verifica se a aplicação está viva e não travada"
    )
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> live() {
        Map<String, String> liveness = new HashMap<>();
        liveness.put("status", "alive");
        liveness.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(liveness);
    }
}
