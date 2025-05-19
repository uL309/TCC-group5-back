package puc.airtrack.airtrack.Pecas;

import java.util.Date;

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
    private String num_serie;
    private Date data_aquisicao;
    private String status;
    private String categoria;
    private int id_engenheiro;
    private Integer fornecedorId; // Use only the Fornecedor ID for DTO
}