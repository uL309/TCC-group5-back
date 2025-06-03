package puc.airtrack.airtrack.Login;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserDTO {
    @NotBlank(message = "id is mandatory")
    @JsonProperty("id")
    private Integer ID_Engenheiro;

    @JsonProperty("nome")
    private String Nome_Engenheiro;

    @JsonProperty("email")
    private String Email_Engenheiro;
    
    @JsonProperty("senha")
    private String Senha_Engenheiro;

    @JsonProperty("role")
    private UserRole Role_Engenheiro;

    @JsonProperty("status")
    private Boolean Status_Engenheiro;

    @JsonProperty("primeiroAcesso")
    private Boolean Primeiro_Acesso;

    @JsonProperty("cpf")
    private String Cpf_Engenheiro;
}
