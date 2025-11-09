package puc.airtrack.airtrack.OrdemDeServico;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EngenheiroStatsDTO {
    @JsonProperty("total_os")
    private int totalOs;
    
    @JsonProperty("os_em_andamento")
    private int osEmAndamento;
    
    @JsonProperty("os_pendentes")
    private int osPendentes;
    
    @JsonProperty("os_concluidas")
    private int osConcluidas;
    
    @JsonProperty("tempo_total_trabalhado")
    private float tempoTotalTrabalhado;
    
    @JsonProperty("tempo_total_esta_semana")
    private float tempoTotalEstaSemana;
    
    @JsonProperty("os_completadas_este_mes")
    private int osCompletadasEsteMes;
}

