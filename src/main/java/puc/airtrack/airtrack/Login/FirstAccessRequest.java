package puc.airtrack.airtrack.Login;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FirstAccessRequest {

    @NotBlank(message = "CPF is required")
    private String cpf;

    @NotBlank(message = "New password is required")
    private String newPassword;
}