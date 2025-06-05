package puc.airtrack.airtrack.Login;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserDTO {
    @NotNull(message = "id is mandatory")
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("role")
    private UserRole role;

    @JsonProperty("status")
    private Boolean status;

    @JsonProperty("firstAccess")
    private Boolean firstAccess;

    @JsonProperty("cpf")
    private String cpf;
}