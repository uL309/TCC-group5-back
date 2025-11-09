package puc.airtrack.airtrack.OrdemDeServico;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuditorStatsDTO {
    @JsonProperty("total_os")
    private int totalOs;
    
    @JsonProperty("os_concluidas")
    private int osConcluidas;
    
    @JsonProperty("os_em_andamento")
    private int osEmAndamento;
    
    @JsonProperty("os_pendentes")
    private int osPendentes;
    
    @JsonProperty("os_concluidas_este_mes")
    private int osConcluidasEsteMes;
    
    @JsonProperty("os_concluidas_esta_semana")
    private int osConcluidasEstaSemana;
}

