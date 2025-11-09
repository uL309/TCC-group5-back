package puc.airtrack.airtrack.OrdemDeServico;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AdminStatsDTO {
    @JsonProperty("total_os")
    private int totalOs;
    
    @JsonProperty("total_usuarios_ativos")
    private int totalUsuariosAtivos;
    
    @JsonProperty("total_motores_ativos")
    private int totalMotoresAtivos;
    
    @JsonProperty("alertas_criticos")
    private int alertasCriticos;
    
    @JsonProperty("os_em_andamento")
    private int osEmAndamento;
    
    @JsonProperty("os_pendentes")
    private int osPendentes;
    
    @JsonProperty("os_concluidas")
    private int osConcluidas;
    
    @JsonProperty("taxa_conclusao_geral")
    private float taxaConclusaoGeral;
    
    @JsonProperty("motores_tbo_expirado")
    private int motoresTboExpirado;
    
    @JsonProperty("os_pendentes_criticas")
    private int osPendentesCriticas;
}

