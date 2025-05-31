package puc.airtrack.airtrack.OrdemDeServico;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CabecalhoOrdemDTO {
    private Integer id;

    @JsonProperty("cliente")
    private String clienteId; // Only the id of Cliente

    @JsonProperty("motor")
    private String motorId; // Only the id of Motor

    @JsonProperty("data_abertura")
    private String dataAbertura;

    @JsonProperty("data_fechamento")
    private String dataFechamento;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("tempo_usado")
    private float tempoUsado;

    @JsonProperty("status")
    private int status;

    @JsonProperty("supervisor")
    private String supervisorId; // Only the id of Supervisor (User)
}