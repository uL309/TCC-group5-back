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
    private int status;

    @JsonProperty("supervisor")
    private String supervisorId; // Only the id of Supervisor (User)

    @JsonProperty("supervisor_nome")
    private String supervisorNome;

    @JsonProperty("valor_hora")
    private float valorHora;

    @JsonProperty("valor_total")
    public float getValorTotal() {
        return tempoUsado * valorHora;
    }

    @JsonProperty("status_descricao")
    public String getStatusDescricao() {
        return switch (status) {
            case 0 -> "Pendente";
            case 1 -> "Andamento";
            case 2 -> "Concluída";
            default -> "Desconhecido";
        };
    }

    private List<LinhaOrdemDTO> linhas;
}