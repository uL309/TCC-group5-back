package puc.airtrack.airtrack.OrdemDeServico;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class CabecalhoOrdemDTO {
    private Integer id;

    @JsonProperty("cliente")
    private String clienteId; // Only the id of Cliente

    @JsonProperty("clienteNome")
    private String clienteNome;

    @JsonProperty("motor")
    private String motorId; // Only the id of Motor

    @JsonProperty("motorNome")
    private String motorNome;

    @JsonProperty("dataAbertura")
    private String dataAbertura;

    @JsonProperty("dataFechamento")
    private String dataFechamento;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("tempoUsado")
    private float tempoUsado;

    @JsonProperty("status")
    private int status;

    @JsonProperty("supervisor")
    private String supervisorId; // Only the id of Supervisor (User)

    @JsonProperty("supervisorNome")
    private String supervisorNome;

    @JsonProperty("statusDescricao")
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