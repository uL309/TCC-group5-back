package puc.airtrack.airtrack.OrdemDeServico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import puc.airtrack.airtrack.Cliente.Cliente;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Motor.Motor;

@Entity
@Getter
@Setter
@Table(name = "CabecalhoOrdem")
public class CabecalhoOrdem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "Cliente", referencedColumnName = "id", foreignKey = @ForeignKey(name = "Cliente"))
    private Cliente cliente;
    @ManyToOne
    @JoinColumn(name = "Motor", referencedColumnName = "id", foreignKey = @ForeignKey(name = "motor"))
    private Motor numSerieMotor;
    @ManyToOne
    @JoinColumn(name = "Supervisor", referencedColumnName = "id", foreignKey = @ForeignKey(name = "user"))
    private User supervisor;
    @Column(name = "data_abertura")
    @NotBlank(message = "Data de abertura não pode ser vazia")
    private String dataAbertura;
    @Column(name = "data_fechamento")
    private String dataFechamento;
    @Column(name = "descricao")
    private String descricao;
    @Column(name = "tipo")
    private String tipo;
    @Column(name = "Tempo_usado")
    private float tempoUsado;
    @Column(name = "tempo_estimado")
    private int tempoEstimado;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    private OrdemStatus status;
    @Column(name = "valor_hora")
    private Float valorHora;
}
