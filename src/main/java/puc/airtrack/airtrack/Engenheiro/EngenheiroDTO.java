package puc.airtrack.airtrack.Engenheiro;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class EngenheiroDTO {
    private Integer id;
    @NotBlank
    private String nome;
    private String crea;
    private String role;
}
