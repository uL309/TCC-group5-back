package puc.airtrack.airtrack.Fornecedor;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class FornecedorDTO {
    private int id;
    private String name;
    private String email;
    private String contato;
    private String category;
    private Boolean status; 
}
