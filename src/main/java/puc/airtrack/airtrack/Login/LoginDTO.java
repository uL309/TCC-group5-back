package puc.airtrack.airtrack.Login;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Schema(description = "Credenciais de login do usuário")
public class LoginDTO {
    
    @Schema(description = "Email/username do usuário", example = "admin@airtrack.com", required = true)
    @NotBlank(message = "Username is mandatory")
    @JsonProperty("username")
    private String username;

    @Schema(description = "Senha do usuário", example = "admin123", required = true)
    @NotBlank(message = "Password is mandatory")
    @JsonProperty("password")
    private String password;

}
