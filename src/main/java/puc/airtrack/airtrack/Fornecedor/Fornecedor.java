package puc.airtrack.airtrack.Fornecedor;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Entity
@Table(name = "Fornecedor")
public class Fornecedor {
    @Id
    @Column(name = "CNPJ")
    private int id;

    @Column(name = "Nome")
    private String name;

    @Column(name = "Email")
    private String email;

    @Column(name = "Contato")
    private String contato;

    @Column(name = "category")
    private String category;

    @Column(name = "Status")
    private Boolean status;
    
}