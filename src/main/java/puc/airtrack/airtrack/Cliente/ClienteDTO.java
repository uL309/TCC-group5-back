package puc.airtrack.airtrack.Cliente;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ClienteDTO {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("cpf")
    private String cpf;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("contato")
    private String contato;

    @JsonProperty("status")
    private Boolean status;
}