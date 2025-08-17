package puc.airtrack.airtrack.OrdemDeServico;

import java.util.List;

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

    @JsonProperty("cliente_nome")
    private String clienteNome;

    @JsonProperty("motor")
    private String motorId; // Only the id of Motor

    @JsonProperty("motor_nome")
    private String motorNome;

    @JsonProperty("data_abertura")
    private String dataAbertura;

    @JsonProperty("data_fechamento")
    private String dataFechamento;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("tempo_usado")
    private float tempoUsado;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("supervisor")
    private String supervisorId; // Only the id of Supervisor (User)

    @JsonProperty("supervisor_nome")
    private String supervisorNome;

    @JsonProperty("valor_hora")
    private Float valorHora;

    @JsonProperty("valor_total")
    public float getValorTotal() {
        return tempoUsado * valorHora;
    }

    @JsonProperty("status_descricao")
    public String getStatusDescricao() {
        if (status == null) return "Desconhecido";
        OrdemStatus ordemStatus = null;
        for (OrdemStatus os : OrdemStatus.values()) {
            if (os.getStatus() == status) {
                ordemStatus = os;
                break;
            }
        }
        return ordemStatus != null ? ordemStatus.getStatusDescricao() : "Desconhecido";
    }

    private List<LinhaOrdemDTO> linhas;
}