package puc.airtrack.airtrack.Pecas;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PecasDTO {
    private int id;
    @JsonProperty("nome")
    private String nome;
    @JsonProperty("numSerie")
    private String numSerie;

    @JsonProperty("dataAquisicao")
    private LocalDate dataAquisicao;

    @JsonProperty("status")
    private Boolean status;

    @JsonProperty("categoria")
    private String categoria;

    @JsonProperty("idEngenheiro")
    private int id_engenheiro;

    @JsonProperty("fornecedor")
    private String fornecedorId; // Use only the Fornecedor ID for DTO
}