package puc.airtrack.airtrack.Fornecedor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @Column(name = "cnpj")
    private String id;

    @Column(name = "nome")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "contato")
    private String contato;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "status")
    private Boolean status;
    
}