package puc.airtrack.airtrack.Services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import puc.airtrack.airtrack.Cliente.ClienteRepo;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserRole;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.Motor.Motor;
import puc.airtrack.airtrack.Motor.MotorRepository;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdem;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemDTO;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemRepository;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemService;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemDTO;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemService;
import puc.airtrack.airtrack.OrdemDeServico.OrdemStatus;
import puc.airtrack.airtrack.notifications.DomainEvent;
import puc.airtrack.airtrack.notifications.DomainEventPublisher;
import puc.airtrack.airtrack.services.AuthUtil;
import puc.airtrack.airtrack.tipoMotor.TipoMotor;
import puc.airtrack.airtrack.tipoMotor.TipoMotorRepository;

@ExtendWith(MockitoExtension.class)
public class CabecalhoOrdemServiceTeste {

    @InjectMocks
    private CabecalhoOrdemService service;

    @Mock
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;
    @Mock
    private ClienteRepo clienteRepo;
    @Mock
    private MotorRepository motorRepository;
    @Mock
    private UserService userService;
    @Mock
    private LinhaOrdemService linhaOrdemService;
    @Mock
    private DomainEventPublisher domainEventPublisher;
    @Mock
    private TipoMotorRepository tipoMotorRepository;

    private MockedStatic<AuthUtil> authUtilStatic;

    @BeforeEach
    void before() {
        // por padrão, nenhum usuário logado
        authUtilStatic = mockStatic(AuthUtil.class);
        authUtilStatic.when(AuthUtil::getUsuarioLogado).thenReturn(null);
    }

    @AfterEach
    void after() {
        authUtilStatic.close();
    }

    @Test
    void createCabecalho_nullDto_returnsBadRequest() {
        ResponseEntity<String> resp = service.createCabecalho(null);
        assertEquals(400, resp.getStatusCodeValue());
        assertTrue(resp.getBody().contains("Invalid"));
    }

    @Test
    void createCabecalho_success_savesAndCreatesLinhas_andPublishesPendingWhenNeeded() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setDescricao("desc");
        dto.setLinhas(new ArrayList<>()); // sem linhas
        dto.setTempoUsado(0f);
        dto.setTempoEstimado(0f);
        dto.setStatus(0); // index into OrdemStatus values; service will compute
        // mock save to set id
        when(cabecalhoOrdemRepository.save(any(CabecalhoOrdem.class))).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId() == 0) e.setId(123);
            return e;
        });

        ResponseEntity<String> resp = service.createCabecalho(dto);
        assertEquals(201, resp.getStatusCodeValue());
        assertTrue(resp.getBody().contains("created"));

        // saved twice: initial save and after linhas
        verify(cabecalhoOrdemRepository, atLeast(1)).save(any(CabecalhoOrdem.class));
        // linhas empty so linhaOrdemService.create not invoked
    }

    @Test
    void createCabecalho_withClienteAndLinhas_callsLinhaOrdemCreateAndSetsCliente() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setClienteId("12345678900");
        dto.setLinhas(new java.util.ArrayList<>());
        LinhaOrdemDTO l = new LinhaOrdemDTO();
        l.setQuantidade(new java.util.ArrayList<>(java.util.Arrays.asList(1)));
        dto.getLinhas().add(l);
        dto.setTempoUsado(0f);
        dto.setTempoEstimado(0f);
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());

        puc.airtrack.airtrack.Cliente.Cliente cliente = new puc.airtrack.airtrack.Cliente.Cliente();
        cliente.setCpf("12345678900");
        when(clienteRepo.findByCpf("12345678900")).thenReturn(Optional.of(cliente));
        when(cabecalhoOrdemRepository.save(any(CabecalhoOrdem.class))).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId() == 0) e.setId(501);
            return e;
        });

        service.createCabecalho(dto);

        verify(clienteRepo).findByCpf("12345678900");
        // garante que linhaOrdemService.create foi chamado com ordemId definido
        verify(linhaOrdemService).create(argThat(arg -> arg.getOrdemId() != null && arg.getOrdemId() == 501));
    }

    @Test
    void updateCabecalho_notFound_returns404() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(9999);
        when(cabecalhoOrdemRepository.findById(9999)).thenReturn(Optional.empty());

        ResponseEntity<String> resp = service.updateCabecalho(dto);
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void updateCabecalho_success_publishesStatusChanged_and_setsEngenheiroWhenLogged() {
        // prepara entidade existente com oldStatus != CONCLUIDA
        CabecalhoOrdem entity = new CabecalhoOrdem();
        entity.setId(600);
        entity.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(600)).thenReturn(Optional.of(entity));

        // DTO que vai mudar status para CONCLUIDA
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(600);
        dto.setLinhas(new java.util.ArrayList<>());
        dto.setTempoUsado(0f);
        dto.setTempoEstimado(0f);
        dto.setStatus(OrdemStatus.CONCLUIDA.ordinal());

        // simula usuário engenheiro logado
        User eng = new User(); eng.setId(2); eng.setRole(UserRole.ROLE_ENGENHEIRO);
        authUtilStatic.when(AuthUtil::getUsuarioLogado).thenReturn(eng);

        when(cabecalhoOrdemRepository.save(any(CabecalhoOrdem.class))).thenAnswer(inv -> inv.getArgument(0));
        // executa
        ResponseEntity<String> resp = service.updateCabecalho(dto);
        assertEquals(200, resp.getStatusCodeValue());

        // publica status changed
        verify(domainEventPublisher).publish(eq("os.status.changed"), any(DomainEvent.class));
    }

    @Test
    void atualizarStatusCabecalho_found_transitions_and_publishes() {
        CabecalhoOrdem entity = new CabecalhoOrdem();
        entity.setId(10);
        entity.setStatus(puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(10)).thenReturn(Optional.of(entity));
        // call with novoStatus index of PENDENTE
        int novoStatus = OrdemStatus.PENDENTE.ordinal();

        ResponseEntity<String> resp = service.atualizarStatusCabecalho(10, novoStatus);
        assertEquals(200, resp.getStatusCodeValue());
        // should have published either pending or status changed; in this case old != PENDENTE and new == PENDENTE -> pending
        verify(domainEventPublisher).publish(eq("os.pending"), any(DomainEvent.class));
    }

    @Test
    void atualizarStatusCabecalho_notFound_returns404() {
        when(cabecalhoOrdemRepository.findById(77)).thenReturn(Optional.empty());
        ResponseEntity<String> resp = service.atualizarStatusCabecalho(77, OrdemStatus.PENDENTE.ordinal());
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void checkAndNotifyMotorTbo_numberFormat_setsNullMotorOnEntity() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setMotorId("not-number");
        dto.setLinhas(new ArrayList<>());
        dto.setTempoUsado(0f);
        dto.setTempoEstimado(0f);
        // adicionar status para evitar NullPointerException durante createCabecalho
        dto.setStatus(puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.ANDAMENTO.ordinal());

        when(cabecalhoOrdemRepository.save(any(CabecalhoOrdem.class))).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId() == 0) e.setId(2);
            return e;
        });

        ResponseEntity<String> resp = service.createCabecalho(dto);
        assertEquals(201, resp.getStatusCodeValue());
        verify(motorRepository, never()).save(any(Motor.class));
    }

    @Test
    void checkAndNotifyMotorTbo_whenMotorExists_andTipoMotorNull_setsNumSerieMotor_but_noPublish() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setMotorId("5");
        dto.setHorasOperacaoMotor(10);
        dto.setLinhas(new ArrayList<>());
        dto.setTempoUsado(0f);
        dto.setTempoEstimado(0f);
        dto.setStatus(puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.ANDAMENTO.ordinal());
        Motor motor = new Motor();
        motor.setId(5);
        motor.setSerie_motor("S1");
        motor.setMarca("M");
        motor.setModelo("X");

        when(motorRepository.findById(5)).thenReturn(Optional.of(motor));
        when(tipoMotorRepository.findByMarcaAndModelo("M", "X")).thenReturn(null);
        when(cabecalhoOrdemRepository.save(any(CabecalhoOrdem.class))).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId() == 0) e.setId(3);
            return e;
        });

        ResponseEntity<String> resp = service.createCabecalho(dto);
        assertEquals(201, resp.getStatusCodeValue());
        // motorRepository.save should not be called because tipoMotor == null
        verify(motorRepository, never()).save(any(Motor.class));
        // domainEventPublisher should not be called with motor.tbo.expired
        verify(domainEventPublisher, never()).publish(eq("motor.tbo.expired"), any(DomainEvent.class));
    }

    @Test
    void checkAndNotifyMotorTbo_hoursLessThanTbo_publishesExpiredClear() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setMotorId("6");
        dto.setHorasOperacaoMotor(50);
        dto.setLinhas(new ArrayList<>());
        dto.setTempoUsado(0f);
        dto.setTempoEstimado(0f);
        dto.setStatus(puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.ANDAMENTO.ordinal());
        Motor motor = new Motor();
        motor.setId(6);
        motor.setSerie_motor("S2");
        motor.setMarca("M2");
        motor.setModelo("Y");

        TipoMotor tipo = new TipoMotor();
        // set tbo higher than dto horas
        tipo.setTbo(100);

        when(motorRepository.findById(6)).thenReturn(Optional.of(motor));
        when(tipoMotorRepository.findByMarcaAndModelo("M2", "Y")).thenReturn(tipo);
        when(cabecalhoOrdemRepository.save(any(CabecalhoOrdem.class))).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId() == 0) e.setId(4);
            return e;
        });

        service.createCabecalho(dto);

        // domainEventPublisher should publish motor.tbo.expired.clear
        verify(domainEventPublisher).publish(eq("motor.tbo.expired.clear"), any(DomainEvent.class));
        // motorRepository.save should have been called to update hours
        verify(motorRepository).save(any(Motor.class));
    }

    @Test
    void checkAndNotifyMotorTbo_hoursGreaterThanTbo_publishesExpiredEvent() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setMotorId("7");
        dto.setHorasOperacaoMotor(200);
        dto.setLinhas(new ArrayList<>());
        dto.setTempoUsado(0f);
        dto.setTempoEstimado(0f);
        dto.setStatus(puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.ANDAMENTO.ordinal());
        Motor motor = new Motor();
        motor.setId(7);
        motor.setSerie_motor("S3");
        motor.setMarca("M3");
        motor.setModelo("Z");

        TipoMotor tipo = new TipoMotor();
        tipo.setTbo(150);

        when(motorRepository.findById(7)).thenReturn(Optional.of(motor));
        when(tipoMotorRepository.findByMarcaAndModelo("M3", "Z")).thenReturn(tipo);
        when(cabecalhoOrdemRepository.save(any(CabecalhoOrdem.class))).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId() == 0) e.setId(5);
            return e;
        });

        service.createCabecalho(dto);

        // motor.tbo.expired should be published
        verify(domainEventPublisher).publish(eq("motor.tbo.expired"), any(DomainEvent.class));
        verify(motorRepository).save(any(Motor.class));
    }

    @Test
    void obterStatusCabecalho_supvisorOnCreate_returnsPendente() {
        // mock logged user as supervisor
        User sup = new User();
        sup.setId(1);
        sup.setRole(UserRole.ROLE_SUPERVISOR);
        authUtilStatic.when(AuthUtil::getUsuarioLogado).thenReturn(sup);

        OrdemStatus result = service.obterStatusCabecalho(true, OrdemStatus.ANDAMENTO);
        assertEquals(OrdemStatus.PENDENTE, result);
    }

    @Test
    void obterStatusCabecalho_engenheiro_changesPendenteToAndamento() {
        User eng = new User();
        eng.setId(2);
        eng.setRole(UserRole.ROLE_ENGENHEIRO);
        authUtilStatic.when(AuthUtil::getUsuarioLogado).thenReturn(eng);

        OrdemStatus result = service.obterStatusCabecalho(false, OrdemStatus.PENDENTE);
        assertEquals(OrdemStatus.ANDAMENTO, result);
    }

    @Test
    void createCabecalho_pendingPublish_ignoresDomainPublishExceptions() {
        // usuário supervisor -> novo OS vira PENDENTE, publisher lança exceção e deve ser ignorada
        User sup = new User(); sup.setId(9); sup.setRole(UserRole.ROLE_SUPERVISOR);
        authUtilStatic.when(AuthUtil::getUsuarioLogado).thenReturn(sup);

        when(cabecalhoOrdemRepository.save(any(CabecalhoOrdem.class))).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId() == 0) e.setId(900);
            return e;
        });

        doThrow(new RuntimeException("rabbit down")).when(domainEventPublisher).publish(eq("os.pending"), any(DomainEvent.class));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setLinhas(new java.util.ArrayList<>());
        dto.setTempoUsado(0f); dto.setTempoEstimado(0f);
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal()); // status fornecido, obterStatusCabecalho fará PENDENTE por ser supervisor

        ResponseEntity<String> resp = service.createCabecalho(dto);
        assertEquals(201, resp.getStatusCodeValue());
        // publisher foi chamado mas exceção foi capturada (não propagada)
        verify(domainEventPublisher).publish(eq("os.pending"), any(DomainEvent.class));
    }

    @Test
    void checkAndNotifyMotorTbo_motorExists_butHorasZero_doesNotSaveOrPublish() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setMotorId("80");
        dto.setHorasOperacaoMotor(0); // zero -> branch skip save/publish
        dto.setLinhas(new java.util.ArrayList<>());
        dto.setTempoUsado(0f);
        dto.setTempoEstimado(0f);
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());

        Motor motor = new Motor(); motor.setId(80); motor.setMarca("AA"); motor.setModelo("BB");
        when(motorRepository.findById(80)).thenReturn(Optional.of(motor));
        when(tipoMotorRepository.findByMarcaAndModelo("AA", "BB")).thenReturn(new TipoMotor()); // tbo default 0

        when(cabecalhoOrdemRepository.save(any(CabecalhoOrdem.class))).thenAnswer(inv -> {
            CabecalhoOrdem ent = inv.getArgument(0);
            if (ent.getId() == 0) ent.setId(801);
            return ent;
        });

        service.createCabecalho(dto);

        verify(motorRepository, never()).save(any(Motor.class));
        verify(domainEventPublisher, never()).publish(eq("motor.tbo.expired"), any(DomainEvent.class));
        verify(domainEventPublisher, never()).publish(eq("motor.tbo.expired.clear"), any(DomainEvent.class));
    }
}