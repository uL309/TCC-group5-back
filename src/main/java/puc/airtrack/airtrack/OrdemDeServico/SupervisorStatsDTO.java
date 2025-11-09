package puc.airtrack.airtrack.OrdemDeServico;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SupervisorStatsDTO {
    @JsonProperty("total_motores")
    private int totalMotores;
    
    @JsonProperty("motores_tbo_proximo")
    private int motoresTboProximo;
    
    @JsonProperty("motores_tbo_expirado")
    private int motoresTboExpirado;
    
    @JsonProperty("os_pendentes")
    private int osPendentes;
    
    @JsonProperty("os_em_andamento")
    private int osEmAndamento;
    
    @JsonProperty("os_concluidas_mes")
    private int osConcluidasMes;
    
    @JsonProperty("total_fornecedores")
    private int totalFornecedores;
}

