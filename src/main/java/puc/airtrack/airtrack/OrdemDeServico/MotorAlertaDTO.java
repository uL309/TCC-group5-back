package puc.airtrack.airtrack.OrdemDeServico;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MotorAlertaDTO {
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("serie_motor")
    private String serieMotor;
    
    @JsonProperty("marca")
    private String marca;
    
    @JsonProperty("modelo")
    private String modelo;
    
    @JsonProperty("horas_operacao")
    private int horasOperacao;
    
    @JsonProperty("tbo")
    private int tbo;
    
    @JsonProperty("percentual_tbo")
    private float percentualTbo;
    
    @JsonProperty("status_alerta")
    private String statusAlerta; // "EXPIrado", "PROXIMO", "OK"
    
    @JsonProperty("cliente_nome")
    private String clienteNome;
}

