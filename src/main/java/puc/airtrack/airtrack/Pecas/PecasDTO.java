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
    @JsonProperty("num_serie")
    private String num_serie;

    @JsonProperty("data_aquisicao")
    private LocalDate data_aquisicao;

    @JsonProperty("status")
    private Boolean status;

    @JsonProperty("categoria")
    private String categoria;

    @JsonProperty("id_engenheiro")
    private int id_engenheiro;

    @JsonProperty("fornecedor")
    private String fornecedorId; // Use only the Fornecedor ID for DTO
}