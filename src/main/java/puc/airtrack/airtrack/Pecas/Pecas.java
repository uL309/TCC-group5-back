package puc.airtrack.airtrack.Pecas;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import puc.airtrack.airtrack.Fornecedor.Fornecedor;

@Data
@Getter
@Setter
@Entity
@Table(name = "Pecas")
public class Pecas {
    
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "num_serie")
    private String num_serie;

    @Column(name = "data_aquisicao")
    private LocalDate data_aquisicao;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "id_engenheiro")
    private int id_engenheiro;

    @ManyToOne/*pegar exemplo caso necessário */
    @JoinColumn(name = "fornecedor", referencedColumnName = "CNPJ", foreignKey = @jakarta.persistence.ForeignKey(name = "fornecedor"))
    private Fornecedor fornecedor;

}
