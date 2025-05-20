package puc.airtrack.airtrack.Cliente;

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
@Table(name = "cliente")
public class Cliente {
    @Id
    @Column(name = "CPF")
    private String id;

    @Column(name = "Nome")
    private String name;

    @Column(name = "Email")
    private String email;

    @Column(name = "Contato")
    private String contato;

    @Column(name = "Status")
    private Boolean status;
}