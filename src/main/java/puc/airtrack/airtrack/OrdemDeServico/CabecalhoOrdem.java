package puc.airtrack.airtrack.OrdemDeServico;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Setter;
import puc.airtrack.airtrack.Cliente.Cliente;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Motor.Motor;
import lombok.Getter;

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
    @Column(name = "Tempo_usado")
    private float tempoUsado;
    @Column(name = "status")
    @Max(value = 2, message = "Status deve ser 0, 1 ou 2")
    @Min(value = 0, message = "Status deve ser 0, 1 ou 2")
    private int status;
    @Column(name = "valor_hora")
    private Float valorHora;
}
