package puc.airtrack.airtrack.Pecas;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import puc.airtrack.airtrack.Fornecedor.Fornecedor;
import jakarta.persistence.GeneratedValue;

@Data
@Getter
@Setter
@Entity
@Table(name = "Pecas")
public class Pecas {
    
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(name = "Nome")
    private String nome;

    @Column(name = "num_serie")
    private String num_serie;

    @Column(name = "data_aquisicao")
    private Date data_aquisicao;

    @Column(name = "status")
    private String status;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "id_engenheiro")
    private int id_engenheiro;

    @ManyToOne/*pegar exemplo caso necessário */
    @JoinColumn(name = "Fornecedor", referencedColumnName = "CNPJ", foreignKey = @jakarta.persistence.ForeignKey(name = "Fornecedor"))
    private Fornecedor fornecedor;

}
