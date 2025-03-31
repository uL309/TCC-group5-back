package puc.airtrack.airtrack.Login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserDTO {
    private Integer id;
    @NotBlank
    private String username;
    private String password;
}
