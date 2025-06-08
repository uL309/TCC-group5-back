package puc.airtrack.airtrack.Login;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserDTO {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("nome")
    private String name;

    @JsonProperty("email")
    private String username;

    @JsonProperty("senha")
    private String password;

    @JsonProperty("role")
    private UserRole role;

    @JsonProperty("status")
    private Boolean status;

    @JsonProperty("primeiro_acesso")
    private Boolean firstAccess;

    @JsonProperty("cpf")
    private String cpf;
}