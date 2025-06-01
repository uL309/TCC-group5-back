package puc.airtrack.airtrack.OrdemDeServico;

import java.util.ArrayList;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.ForeignKey;
import lombok.Getter;
import lombok.Setter;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Pecas.Pecas;

@Entity
@Getter
@Setter
@Table(name = "LinhaOrdem")
public class LinhaOrdem {
    
    @Id
    private int id;
    @ManyToOne
    @JoinColumn(name = "cabecalho", referencedColumnName = "id", foreignKey = @ForeignKey(name = "cabecalho"))
    private CabecalhoOrdem Ordem;
    @ManyToOne
    @JoinColumn(name = "peca", referencedColumnName = "id", foreignKey = @ForeignKey(name = "peca"))
    private Pecas peca;
    @OneToOne
    @JoinColumn(name = "Engenheiro", referencedColumnName = "ID_Engenheiro", foreignKey = @ForeignKey(name = "engenheiro"))
    private User engenheiro;
    private ArrayList<Integer> quantidade;
    private float tempoGasto;
}
