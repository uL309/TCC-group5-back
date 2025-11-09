package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import puc.airtrack.airtrack.Fornecedor.Fornecedor;
import puc.airtrack.airtrack.Fornecedor.FornecedorRepo;
import puc.airtrack.airtrack.Motor.Motor;
import puc.airtrack.airtrack.Motor.MotorRepository;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdem;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemController;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemRepository;
import puc.airtrack.airtrack.OrdemDeServico.OrdemStatus;
import puc.airtrack.airtrack.OrdemDeServico.SupervisorStatsDTO;
import puc.airtrack.airtrack.tipoMotor.TipoMotor;
import puc.airtrack.airtrack.tipoMotor.TipoMotorRepository;

@ExtendWith(MockitoExtension.class)
class CabecalhoOrdemControllerSupervisorStatsTest {

    @Mock
    private MotorRepository motorRepository;
    @Mock
    private TipoMotorRepository tipoMotorRepository;
    @Mock
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;
    @Mock
    private FornecedorRepo fornecedorRepo;

    @InjectMocks
    private CabecalhoOrdemController controller;

    private Motor motor(int id, boolean ativo, int horas) {
        Motor m = new Motor();
        m.setId(id);
        m.setStatus(ativo);
        m.setMarca("X");
        m.setModelo("M");
        m.setHoras_operacao(horas);
        m.setSerie_motor("S" + id);
        return m;
    }

    private CabecalhoOrdem os(int id, OrdemStatus status, String abertura, String fechamento) {
        CabecalhoOrdem c = new CabecalhoOrdem();
        c.setId(id);
        c.setStatus(status);
        c.setDataAbertura(abertura);
        c.setDataFechamento(fechamento);
        return c;
    }

    @Test
    void getEstatisticasSupervisor_ok() {
        // Motores (TBO=100): 79% (ignorado), 80% (proximo), 100% (expirado), inativo
        Motor m1 = motor(1, true, 79);
        Motor m2 = motor(2, true, 80);
        Motor m3 = motor(3, true, 100);
        Motor m4 = motor(4, false, 150); // inativo
        when(motorRepository.findAll()).thenReturn(List.of(m1, m2, m3, m4));

        TipoMotor tipo = new TipoMotor();
        tipo.setTbo(100);
        when(tipoMotorRepository.findByMarcaAndModelo("X", "M")).thenReturn(tipo);

        // OS
        CabecalhoOrdem pend1 = os(10, OrdemStatus.PENDENTE, LocalDate.now().minusDays(5).toString(), null);
        CabecalhoOrdem pend2 = os(11, OrdemStatus.PENDENTE, LocalDate.now().minusDays(2).toString(), null);
        CabecalhoOrdem andamento1 = os(12, OrdemStatus.ANDAMENTO, LocalDate.now().minusDays(1).toString(), null);
        CabecalhoOrdem concluidaMes = os(13, OrdemStatus.CONCLUIDA, LocalDate.now().minusDays(7).toString(), LocalDate.now().toString());

        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.PENDENTE)).thenReturn(List.of(pend1, pend2));
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.ANDAMENTO)).thenReturn(List.of(andamento1));
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.CONCLUIDA)).thenReturn(List.of(concluidaMes));

        // Fornecedores
        when(fornecedorRepo.findAll()).thenReturn(List.of(new Fornecedor(), new Fornecedor(), new Fornecedor()));

        ResponseEntity<SupervisorStatsDTO> resp = controller.getEstatisticasSupervisor();
        SupervisorStatsDTO stats = resp.getBody();

        assertEquals(200, resp.getStatusCode().value());
        assertEquals(3, stats.getTotalMotores());        // ativos
        assertEquals(1, stats.getMotoresTboProximo());  // 80%
        assertEquals(1, stats.getMotoresTboExpirado()); // 100%
        assertEquals(2, stats.getOsPendentes());
        assertEquals(1, stats.getOsEmAndamento());
        assertEquals(1, stats.getOsConcluidasMes());
        assertEquals(3, stats.getTotalFornecedores());
    }
}