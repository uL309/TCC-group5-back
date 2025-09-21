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

    @JsonProperty("tipo")
    private String tipo;

    @JsonProperty("tempo_usado")
    private float tempoUsado;

    @JsonProperty("tempo_estimado")
    private float tempoEstimado;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("supervisor")
    private String supervisorId; // Only the id of Supervisor (User)

    @JsonProperty("engenheiro_atuante")
    private String engenheiroAtuanteId; // Only the id of Engenheiro Atuante (User)

    @JsonProperty("supervisor_nome")
    private String supervisorNome;

    @JsonProperty("valor_hora")
    private Float valorHora;

    @JsonProperty("valor_total")
    public float getValorTotal() {
        return tempoUsado * valorHora;
    }

    @JsonProperty("engenheiro_atuante_nome")
    private String engenheiroAtuanteNome;

    @JsonProperty("horas_operacao_motor")
    private int horasOperacaoMotor;

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