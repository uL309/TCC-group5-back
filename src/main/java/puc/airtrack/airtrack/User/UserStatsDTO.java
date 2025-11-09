package puc.airtrack.airtrack.User;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserStatsDTO {
    @JsonProperty("total_usuarios")
    private int totalUsuarios;
    
    @JsonProperty("usuarios_ativos")
    private int usuariosAtivos;
    
    @JsonProperty("usuarios_engenheiro")
    private int usuariosEngenheiro;
    
    @JsonProperty("usuarios_auditor")
    private int usuariosAuditor;
    
    @JsonProperty("usuarios_supervisor")
    private int usuariosSupervisor;
    
    @JsonProperty("usuarios_admin")
    private int usuariosAdmin;
}

