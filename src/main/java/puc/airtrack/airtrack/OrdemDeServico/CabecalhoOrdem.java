package puc.airtrack.airtrack.OrdemDeServico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Setter;
import puc.airtrack.airtrack.Cliente.Cliente;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Motor.Motor;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "CabecalhoOrdem")
public class CabecalhoOrdem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToOne
    @JoinColumn(name = "cliente", referencedColumnName = "CPF", foreignKey = @ForeignKey(name = "cliente"))
    private Cliente cliente;
    @OneToOne
    @JoinColumn(name = "Motor", referencedColumnName = "id", foreignKey = @ForeignKey(name = "motor"))
    private Motor numSerieMotor;
    @OneToOne
    @JoinColumn(name = "Supervisor", referencedColumnName = "ID_Engenheiro", foreignKey = @ForeignKey(name = "user"))
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
    @NotBlank(message = "Status não pode ser vazio")
    @Max(value = 2, message = "Status deve ser 0, 1 ou 2")
    @Min(value = 0, message = "Status deve ser 0, 1 ou 2")
    private int status;
}
