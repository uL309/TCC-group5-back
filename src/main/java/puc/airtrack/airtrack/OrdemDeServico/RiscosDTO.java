package puc.airtrack.airtrack.OrdemDeServico;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RiscosDTO {
    @JsonProperty("motores_tbo_expirado")
    private int motoresTboExpirado;
    
    @JsonProperty("os_pendentes_criticas")
    private int osPendentesCriticas;
    
    @JsonProperty("taxa_conclusao_baixa")
    private boolean taxaConclusaoBaixa;
    
    @JsonProperty("taxa_conclusao")
    private float taxaConclusao;
    
    @JsonProperty("total_riscos")
    private int totalRiscos;
}

