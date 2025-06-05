package puc.airtrack.airtrack.OrdemDeServico;

import java.util.ArrayList;

import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "cabecalho", referencedColumnName = "id", foreignKey = @ForeignKey(name = "cabecalho"))
    private CabecalhoOrdem ordem;
    @ManyToOne
    @JoinColumn(name = "peca", referencedColumnName = "id", foreignKey = @ForeignKey(name = "peca"))
    private Pecas peca;
    @OneToOne
    @JoinColumn(name = "Engenheiro", referencedColumnName = "id", foreignKey = @ForeignKey(name = "engenheiro"))
    private User engenheiro;
    private ArrayList<Integer> quantidade;
    private float tempoGasto;
}
