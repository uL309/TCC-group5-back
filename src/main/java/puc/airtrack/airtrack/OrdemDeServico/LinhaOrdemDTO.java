package puc.airtrack.airtrack.OrdemDeServico;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Data
@Getter
@Setter
public class LinhaOrdemDTO {
    private int id;

    @JsonProperty("cabecalho")
    private Integer ordemId;

    @JsonProperty("peca")
    private Integer pecaId;

    @JsonProperty("peca_nome")
    private String pecaNome;

    @JsonProperty("quantidade")
    private ArrayList<Integer> quantidade;

    @JsonProperty("tempo_gasto")
    private float tempoGasto;

    @JsonProperty("engenheiro")
    private String engenheiroId;
}