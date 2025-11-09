package puc.airtrack.airtrack.OrdemDeServico;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AlertaConformidadeDTO {
    @JsonProperty("tipo")
    private String tipo; // "TBO_EXPIRADO", "OS_PENDENTE_CRITICA", "OS_TEMPO_EXCEDIDO"
    
    @JsonProperty("severidade")
    private String severidade; // "CRITICO", "ALTO", "MEDIO"
    
    @JsonProperty("titulo")
    private String titulo;
    
    @JsonProperty("descricao")
    private String descricao;
    
    @JsonProperty("motor_id")
    private Integer motorId;
    
    @JsonProperty("motor_serie")
    private String motorSerie;
    
    @JsonProperty("os_id")
    private Integer osId;
    
    @JsonProperty("data")
    private String data;
}

