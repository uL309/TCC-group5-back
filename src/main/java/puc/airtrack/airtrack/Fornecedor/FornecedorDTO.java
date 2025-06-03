package puc.airtrack.airtrack.Fornecedor;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class FornecedorDTO {

    @JsonProperty("cnpj")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("contato")
    private String contato;

    @JsonProperty("categoria")
    private String categoria;

    @JsonProperty("status")
    private Boolean status; 
}
