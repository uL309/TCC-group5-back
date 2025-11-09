package puc.airtrack.airtrack.logs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LogEntryDTO {
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("controller_method")
    private String controllerMethod;
    
    @JsonProperty("module")
    private String module;
}

