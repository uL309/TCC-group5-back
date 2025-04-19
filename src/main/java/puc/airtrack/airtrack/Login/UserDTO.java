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
    @JsonProperty("ID_Engenheiro")
    private Integer ID_Engenheiro;

    @JsonProperty("Nome_Engenheiro")
    private String Nome_Engenheiro;

    @JsonProperty("Email_Engenheiro")
    private String Email_Engenheiro;
    
    @JsonProperty("Senha_Engenheiro")
    private String Senha_Engenheiro;

    @JsonProperty("Role_Engenheiro")
    private int Role_Engenheiro;

    @JsonProperty("Status_Engenheiro")
    private Boolean Status_Engenheiro;
}
