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
    private String name;
    @NotBlank
    private String username;
    private String password;
    private int role;
    private Boolean status;
}
