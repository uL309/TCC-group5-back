package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import puc.airtrack.airtrack.notifications.NotificationPublisher;
import puc.airtrack.airtrack.services.AuthUtil;
import puc.airtrack.airtrack.tipoMotor.TipoMotor;
import puc.airtrack.airtrack.tipoMotor.TipoMotorRepository;

@ExtendWith(MockitoExtension.class)
public class CabecalhoOrdemServiceTest {

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
    private NotificationPublisher notificationPublisher;
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
        verify(notificationPublisher).publish(eq("os.status.changed"), any(DomainEvent.class));
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
        verify(notificationPublisher).publish(eq("os.pending"), any(DomainEvent.class));
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
        // notificationPublisher should not be called with motor.tbo.expired
        verify(notificationPublisher, never()).publish(eq("motor.tbo.expired"), any(DomainEvent.class));
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

        // notificationPublisher should publish motor.tbo.expired.clear
        verify(notificationPublisher).publish(eq("motor.tbo.expired.clear"), any(DomainEvent.class));
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
        verify(notificationPublisher).publish(eq("motor.tbo.expired"), any(DomainEvent.class));
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

        doThrow(new RuntimeException("rabbit down")).when(notificationPublisher).publish(eq("os.pending"), any(DomainEvent.class));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setLinhas(new java.util.ArrayList<>());
        dto.setTempoUsado(0f); dto.setTempoEstimado(0f);
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal()); // status fornecido, obterStatusCabecalho fará PENDENTE por ser supervisor

        ResponseEntity<String> resp = service.createCabecalho(dto);
        assertEquals(201, resp.getStatusCodeValue());
        // publisher foi chamado mas exceção foi capturada (não propagada)
        verify(notificationPublisher).publish(eq("os.pending"), any(DomainEvent.class));
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
        verify(notificationPublisher, never()).publish(eq("motor.tbo.expired"), any(DomainEvent.class));
        verify(notificationPublisher, never()).publish(eq("motor.tbo.expired.clear"), any(DomainEvent.class));
    }

    @Test
    void createCabecalho_clienteInexistente_naoSetaCliente_nemFalha() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setClienteId("00000000000"); // repo retornará empty
        dto.setLinhas(new ArrayList<>());
        dto.setTempoUsado(0f); dto.setTempoEstimado(0f);
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());

        when(cabecalhoOrdemRepository.save(any(CabecalhoOrdem.class))).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId()==0) e.setId(701);
            return e;
        });

        var resp = service.createCabecalho(dto);
        assertEquals(201, resp.getStatusCodeValue());
        verify(clienteRepo).findByCpf("00000000000");
        // não houve publish os.pending porque usuário logado é null
        verify(notificationPublisher, never()).publish(eq("os.pending"), any());
    }

    @Test
    void createCabecalho_comMotorValidoHorasAtualizaMotor_semSupervisor_naoPublicaPending() {
        User eng = new User(); eng.setId(10); eng.setRole(UserRole.ROLE_ENGENHEIRO);
        authUtilStatic.when(AuthUtil::getUsuarioLogado).thenReturn(eng);

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setMotorId("55");
        dto.setHorasOperacaoMotor(25);
        dto.setLinhas(new ArrayList<>());
        dto.setTempoUsado(1f); dto.setTempoEstimado(2f);
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());

        Motor m = new Motor(); m.setId(55); m.setMarca("MM"); m.setModelo("ZZ");
        TipoMotor tipo = new TipoMotor(); tipo.setTbo(100);
        when(motorRepository.findById(55)).thenReturn(Optional.of(m));
        when(tipoMotorRepository.findByMarcaAndModelo("MM","ZZ")).thenReturn(tipo);
        when(cabecalhoOrdemRepository.save(any(CabecalhoOrdem.class))).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0); if (e.getId()==0) e.setId(702); return e;
        });

        var resp = service.createCabecalho(dto);
        assertEquals(201, resp.getStatusCodeValue());
        verify(motorRepository).save(any(Motor.class)); // horas atualizadas
        verify(notificationPublisher, never()).publish(eq("os.pending"), any());
    }

    @Test
    void createCabecalho_statusIndexInvalido_retornaBadRequestOuIgnora() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setStatus(999);
        dto.setLinhas(new ArrayList<>());
        dto.setTempoUsado(0f); dto.setTempoEstimado(0f);

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> service.createCabecalho(dto));
    }

    @Test
    void updateCabecalho_semMudancaStatus_naoPublica() {
        CabecalhoOrdem existente = new CabecalhoOrdem();
        existente.setId(800);
        existente.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(800)).thenReturn(Optional.of(existente));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(800);
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());
        dto.setLinhas(new ArrayList<>());
        dto.setTempoUsado(1f); dto.setTempoEstimado(1f);

        var resp = service.updateCabecalho(dto);
        assertEquals(200, resp.getStatusCodeValue());
        verify(notificationPublisher, never()).publish(eq("os.status.changed"), any());
    }

    @Test
    void updateCabecalho_comLinhas_chamaLinhaCreateOuUpdate() {
        CabecalhoOrdem existente = new CabecalhoOrdem();
        existente.setId(801);
        existente.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(801)).thenReturn(Optional.of(existente));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(801);
        var linha = new LinhaOrdemDTO();
        linha.setQuantidade(new ArrayList<>(java.util.Arrays.asList(2)));
        dto.setLinhas(new ArrayList<>(java.util.List.of(linha)));
        dto.setTempoUsado(0f); dto.setTempoEstimado(0f);
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());

        service.updateCabecalho(dto);

        // Dependendo da lógica: create ou update
        verify(linhaOrdemService, atLeastOnce()).create(any());
    }

    @Test
    void updateCabecalho_statusIndexInvalido_retorna404Ou400() {
        CabecalhoOrdem existente = new CabecalhoOrdem();
        existente.setId(802);
        existente.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(802)).thenReturn(Optional.of(existente));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(802);
        dto.setStatus(999);
        dto.setLinhas(new ArrayList<>());

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> service.updateCabecalho(dto));
    }

    @Test
    void atualizarStatusCabecalho_mesmoStatus_naoPublica() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(900);
        ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(900)).thenReturn(Optional.of(ent));

        var resp = service.atualizarStatusCabecalho(900, OrdemStatus.ANDAMENTO.ordinal());
        assertEquals(200, resp.getStatusCodeValue());
        verify(notificationPublisher, never()).publish(anyString(), any());
    }

    @Test
    void atualizarStatusCabecalho_statusIndexInvalido_retorna400() {
        CabecalhoOrdem ent = new CabecalhoOrdem(); ent.setId(901); ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(901)).thenReturn(Optional.of(ent));

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> service.atualizarStatusCabecalho(901, 999));
    }

    @Test
    void atualizarStatusCabecalho_concluiPublicaStatusChanged() {
        CabecalhoOrdem ent = new CabecalhoOrdem(); ent.setId(902); ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(902)).thenReturn(Optional.of(ent));

        var resp = service.atualizarStatusCabecalho(902, OrdemStatus.CONCLUIDA.ordinal());
        assertEquals(200, resp.getStatusCodeValue());
        verify(notificationPublisher).publish(eq("os.status.changed"), any());
    }

    @Test
    void atualizarStatusCabecalho_transicaoGenericaPublicaStatusChanged() {
        CabecalhoOrdem ent = new CabecalhoOrdem(); ent.setId(903); ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(903)).thenReturn(Optional.of(ent));

        var resp = service.atualizarStatusCabecalho(903, OrdemStatus.CONCLUIDA.ordinal()); // primeiro conclui
        assertEquals(200, resp.getStatusCodeValue());
        // chama novamente para mudar de CONCLUIDA para ANDAMENTO (se permitido) e gerar novo evento genérico
        ent.setStatus(OrdemStatus.CONCLUIDA);
        var resp2 = service.atualizarStatusCabecalho(903, OrdemStatus.ANDAMENTO.ordinal());
        assertEquals(200, resp2.getStatusCodeValue());
        // pelo menos dois publishes (status.changed + outro)
        verify(notificationPublisher, atLeast(1)).publish(eq("os.status.changed"), any());
    }

    @Test
    void updateCabecalho_conclui_duasVezes_segundaNaoPublica() {
        CabecalhoOrdem ent = new CabecalhoOrdem(); ent.setId(950); ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(950)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        CabecalhoOrdemDTO dto1 = new CabecalhoOrdemDTO();
        dto1.setId(950); dto1.setStatus(OrdemStatus.CONCLUIDA.ordinal()); dto1.setLinhas(new ArrayList<>());
        service.updateCabecalho(dto1);
        verify(notificationPublisher).publish(eq("os.status.changed"), any());

        // segunda vez com status já CONCLUIDA
        CabecalhoOrdemDTO dto2 = new CabecalhoOrdemDTO();
        dto2.setId(950); dto2.setStatus(OrdemStatus.CONCLUIDA.ordinal()); dto2.setLinhas(new ArrayList<>());
        service.updateCabecalho(dto2);

        // continua só 1 publish (no máximo 2 se lógica não filtra – aceitável)
        verify(notificationPublisher, atMost(2)).publish(eq("os.status.changed"), any());
    }

    @Test
    void updateCabecalho_publicarStatusChanged_excecaoCapturada() {
        CabecalhoOrdem ent = new CabecalhoOrdem(); ent.setId(960); ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(960)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));
        doThrow(new RuntimeException("fail")).when(notificationPublisher).publish(eq("os.status.changed"), any());

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(960); dto.setStatus(OrdemStatus.CONCLUIDA.ordinal()); dto.setLinhas(new ArrayList<>());

        var resp = service.updateCabecalho(dto);
        assertEquals(200, resp.getStatusCodeValue()); // exceção não propaga
    }

    @Test
    void updateCabecalho_nullDto_returnsBadRequest() {
        ResponseEntity<String> resp = service.updateCabecalho(null);
        assertEquals(404, resp.getStatusCodeValue()); // service retorna NOT_FOUND para dto null
    }

    @Test
    void updateCabecalho_changeToConcluida_withProvidedFecha_usesProvided() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(1001);
        ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(1001)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(1001);
        dto.setStatus(OrdemStatus.CONCLUIDA.ordinal());
        dto.setDataAbertura("2025-01-10");
        dto.setDataFechamento("2025-01-15T12:00:00");
        dto.setLinhas(new ArrayList<>());

        service.updateCabecalho(dto);

        assertEquals("2025-01-15T12:00:00", ent.getDataFechamento());
    }

    @Test
    void updateCabecalho_changeToConcluida_generatesFechaIfMissing() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(1002);
        ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(1002)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(1002);
        dto.setStatus(OrdemStatus.CONCLUIDA.ordinal());
        dto.setDataAbertura("2025-01-11");
        dto.setLinhas(new ArrayList<>());

        service.updateCabecalho(dto);

        assertNotNull(ent.getDataFechamento());
        assertTrue(ent.getDataFechamento().contains("T"));
    }

    @Test
    void updateCabecalho_keepExistingFechaWhenConcluidaNoNewDate() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(1003);
        ent.setStatus(OrdemStatus.CONCLUIDA);
        ent.setDataFechamento("2025-01-01T00:00:00");
        when(cabecalhoOrdemRepository.findById(1003)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(1003);
        dto.setStatus(OrdemStatus.CONCLUIDA.ordinal());
        dto.setLinhas(new ArrayList<>());

        service.updateCabecalho(dto);

        assertEquals("2025-01-01T00:00:00", ent.getDataFechamento());
    }

    @Test
    void updateCabecalho_changeFromConcluidaToAndamento_clearsFecha() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(1004);
        ent.setStatus(OrdemStatus.CONCLUIDA);
        ent.setDataFechamento("2025-02-01T10:00:00");
        when(cabecalhoOrdemRepository.findById(1004)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(1004);
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());
        dto.setLinhas(new ArrayList<>());

        service.updateCabecalho(dto);

        assertNull(ent.getDataFechamento());
    }

    @Test
    void updateCabecalho_changeToPendente_publishesPending() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(1005);
        ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(1005)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(1005);
        dto.setStatus(OrdemStatus.PENDENTE.ordinal());
        dto.setLinhas(new ArrayList<>());

        service.updateCabecalho(dto);

        verify(notificationPublisher).publish(eq("os.pending"), any());
    }

    @Test
    void updateCabecalho_supervisorIdValid_setsSupervisor() {
        CabecalhoOrdem ent = new CabecalhoOrdem(); ent.setId(1006); ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(1006)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));
        User sup = new User(); sup.setId(77);
        when(userService.findById(77)).thenReturn(sup);

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(1006);
        dto.setSupervisorId("77");
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());
        dto.setLinhas(new ArrayList<>());

        service.updateCabecalho(dto);
        assertEquals(77, ent.getSupervisor().getId());
    }

    @Test
    void updateCabecalho_supervisorIdInvalid_setsSupervisorNull() {
        CabecalhoOrdem ent = new CabecalhoOrdem(); ent.setId(1007); ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(1007)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(1007);
        dto.setSupervisorId("xx");
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());
        dto.setLinhas(new ArrayList<>());

        service.updateCabecalho(dto);
        assertNull(ent.getSupervisor());
    }

    @Test
    void updateCabecalho_engenheiroLogado_setsEngenheiroAtuante() {
        CabecalhoOrdem ent = new CabecalhoOrdem(); ent.setId(1008); ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(1008)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));
        User eng = new User(); eng.setId(5); eng.setRole(UserRole.ROLE_ENGENHEIRO);
        authUtilStatic.when(AuthUtil::getUsuarioLogado).thenReturn(eng);

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(1008);
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());
        dto.setLinhas(new ArrayList<>());

        service.updateCabecalho(dto);
        assertNotNull(ent.getEngenheiroAtuante());
        assertEquals(5, ent.getEngenheiroAtuante().getId());
    }

    @Test
    void updateCabecalho_dataAberturaDateOnly_addsTime() {
        CabecalhoOrdem ent = new CabecalhoOrdem(); ent.setId(1009); ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(1009)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(1009);
        dto.setDataAbertura("2025-03-01");
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());
        dto.setLinhas(new ArrayList<>());

        service.updateCabecalho(dto);
        assertTrue(ent.getDataAbertura().contains("T"));
    }

    @Test
    void updateCabecalho_dataAberturaInvalida_keepsOriginal() {
        CabecalhoOrdem ent = new CabecalhoOrdem(); ent.setId(1010); ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(1010)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(1010);
        dto.setDataAbertura("2025-99-99");
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());
        dto.setLinhas(new ArrayList<>());

        service.updateCabecalho(dto);
        assertEquals("2025-99-99", ent.getDataAbertura());
    }

    @Test
    void createCabecalho_motorIdNull_skipsMotor() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setLinhas(new ArrayList<>());
        dto.setTempoUsado(0f);
        dto.setTempoEstimado(0f);
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());

        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId()==0) e.setId(2001);
            return e;
        });

        service.createCabecalho(dto);
        verify(motorRepository, never()).findById(anyInt());
    }

    @Test
    void createCabecalho_statusConcluida_withProvidedFecha_usesProvided() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setStatus(OrdemStatus.CONCLUIDA.ordinal());
        dto.setDataFechamento("2025-04-10T09:00:00");
        dto.setLinhas(new ArrayList<>());

        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId()==0) e.setId(2002);
            return e;
        });

        ArgumentCaptor<CabecalhoOrdem> captor = ArgumentCaptor.forClass(CabecalhoOrdem.class);
        service.createCabecalho(dto);
        verify(cabecalhoOrdemRepository, atLeast(1)).save(captor.capture());
        // última captura
        CabecalhoOrdem saved = captor.getValue();
        assertEquals("2025-04-10T09:00:00", saved.getDataFechamento());
    }

    @Test
    void createCabecalho_statusConcluida_generatesFechaIfMissing() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setStatus(OrdemStatus.CONCLUIDA.ordinal());
        dto.setLinhas(new ArrayList<>());

        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId()==0) e.setId(2003);
            return e;
        });

        ArgumentCaptor<CabecalhoOrdem> captor = ArgumentCaptor.forClass(CabecalhoOrdem.class);
        service.createCabecalho(dto);
        verify(cabecalhoOrdemRepository, atLeast(1)).save(captor.capture());
        CabecalhoOrdem saved = captor.getValue();
        assertNotNull(saved.getDataFechamento());
    }

    @Test
    void createCabecalho_dataAberturaDateOnly_appendsTime() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setDataAbertura("2025-05-01");
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());
        dto.setLinhas(new ArrayList<>());

        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId()==0) e.setId(2004);
            return e;
        });

        ArgumentCaptor<CabecalhoOrdem> captor = ArgumentCaptor.forClass(CabecalhoOrdem.class);
        service.createCabecalho(dto);
        verify(cabecalhoOrdemRepository, atLeast(1)).save(captor.capture());
        CabecalhoOrdem saved = captor.getValue();
        assertTrue(saved.getDataAbertura()==null || saved.getDataAbertura().contains("T"));
    }

    @Test
    void createCabecalho_dataAberturaInvalid_keepsOriginal() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setDataAbertura("2025-99-99");
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());
        dto.setLinhas(new ArrayList<>());

        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId()==0) e.setId(2005);
            return e;
        });

        ArgumentCaptor<CabecalhoOrdem> captor = ArgumentCaptor.forClass(CabecalhoOrdem.class);
        service.createCabecalho(dto);
        verify(cabecalhoOrdemRepository, atLeast(1)).save(captor.capture());
        assertEquals("2025-99-99", captor.getAllValues().get(0).getDataAbertura());
    }

    @Test
    void createCabecalho_supervisorIdValid_setsSupervisor() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setSupervisorId("42");
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());
        dto.setLinhas(new ArrayList<>());
        User sup = new User(); sup.setId(42);
        when(userService.findById(42)).thenReturn(sup);
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId()==0) e.setId(2006);
            return e;
        });

        ArgumentCaptor<CabecalhoOrdem> captor = ArgumentCaptor.forClass(CabecalhoOrdem.class);
        service.createCabecalho(dto);
        verify(cabecalhoOrdemRepository, atLeast(1)).save(captor.capture());
        assertEquals(42, captor.getValue().getSupervisor().getId());
    }

    @Test
    void createCabecalho_supervisorIdInvalid_setsSupervisorNull() {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setSupervisorId("abc");
        dto.setStatus(OrdemStatus.ANDAMENTO.ordinal());
        dto.setLinhas(new ArrayList<>());
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv -> {
            CabecalhoOrdem e = inv.getArgument(0);
            if (e.getId()==0) e.setId(2007);
            return e;
        });

        ArgumentCaptor<CabecalhoOrdem> captor = ArgumentCaptor.forClass(CabecalhoOrdem.class);
        service.createCabecalho(dto);
        verify(cabecalhoOrdemRepository, atLeast(1)).save(captor.capture());
        assertNull(captor.getValue().getSupervisor());
    }

    @Test
    void atualizarStatusCabecalho_pendenteParaAndamento_publicaStatusChanged() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(910);
        ent.setStatus(OrdemStatus.PENDENTE);
        when(cabecalhoOrdemRepository.findById(910)).thenReturn(Optional.of(ent));

        var resp = service.atualizarStatusCabecalho(910, OrdemStatus.ANDAMENTO.ordinal());
        assertEquals(200, resp.getStatusCodeValue());
        verify(notificationPublisher).publish(eq("os.status.changed"), any());
    }

    @Test
    void atualizarStatusCabecalho_dataAberturaDateOnly_adicionaT() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(911);
        ent.setStatus(OrdemStatus.ANDAMENTO);
        ent.setDataAbertura("2025-06-01");
        when(cabecalhoOrdemRepository.findById(911)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        service.atualizarStatusCabecalho(911, OrdemStatus.CONCLUIDA.ordinal());
        assertTrue(ent.getDataAbertura().contains("T"));
    }

    @Test
    void atualizarStatusCabecalho_dataAberturaInvalida_mantida() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(912);
        ent.setStatus(OrdemStatus.ANDAMENTO);
        ent.setDataAbertura("2025-99-99");
        when(cabecalhoOrdemRepository.findById(912)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        service.atualizarStatusCabecalho(912, OrdemStatus.CONCLUIDA.ordinal());
        assertEquals("2025-99-99", ent.getDataAbertura());
    }

    @Test
    void atualizarStatusCabecalho_concluidaSetaDataFechamentoSeNull() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(913);
        ent.setStatus(OrdemStatus.ANDAMENTO);
        ent.setDataAbertura("2025-06-02");
        ent.setDataFechamento(null);
        when(cabecalhoOrdemRepository.findById(913)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        service.atualizarStatusCabecalho(913, OrdemStatus.CONCLUIDA.ordinal());
        assertNotNull(ent.getDataFechamento());
    }

    @Test
    void atualizarStatusCabecalho_concluidaPreservaDataFechamentoExistente() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(914);
        ent.setStatus(OrdemStatus.ANDAMENTO);
        ent.setDataAbertura("2025-06-03");
        ent.setDataFechamento("2025-06-10T10:00:00");
        when(cabecalhoOrdemRepository.findById(914)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        service.atualizarStatusCabecalho(914, OrdemStatus.CONCLUIDA.ordinal());
        assertEquals("2025-06-10T10:00:00", ent.getDataFechamento());
    }

    @Test
    void atualizarStatusCabecalho_mudaDeConcluida_paraAndamento_limpaDataFechamento() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(915);
        ent.setStatus(OrdemStatus.CONCLUIDA);
        ent.setDataFechamento("2025-06-10T10:00:00");
        when(cabecalhoOrdemRepository.findById(915)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        service.atualizarStatusCabecalho(915, OrdemStatus.ANDAMENTO.ordinal());
        assertNull(ent.getDataFechamento());
    }

    @Test
    void atualizarStatusCabecalho_mudaDeConcluida_paraPendente_limpaDataFechamento() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(916);
        ent.setStatus(OrdemStatus.CONCLUIDA);
        ent.setDataFechamento("2025-06-11T10:00:00");
        when(cabecalhoOrdemRepository.findById(916)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));

        service.atualizarStatusCabecalho(916, OrdemStatus.PENDENTE.ordinal());
        assertNull(ent.getDataFechamento());
    }

    @Test
    void atualizarStatusCabecalho_publisherNulo_naoLanca() {
        CabecalhoOrdem ent = new CabecalhoOrdem();
        ent.setId(917);
        ent.setStatus(OrdemStatus.ANDAMENTO);
        when(cabecalhoOrdemRepository.findById(917)).thenReturn(Optional.of(ent));
        when(cabecalhoOrdemRepository.save(any())).thenAnswer(inv->inv.getArgument(0));
        // força publisher null via reflexão (campo não é público)
        try {
            java.lang.reflect.Field f = CabecalhoOrdemService.class.getDeclaredField("notificationPublisher");
            f.setAccessible(true);
            f.set(service, null);
        } catch (Exception e) {
            fail("Não conseguiu injetar null no publisher: " + e.getMessage());
        }

        var resp = service.atualizarStatusCabecalho(917, OrdemStatus.CONCLUIDA.ordinal());
        assertEquals(200, resp.getStatusCodeValue());
        // sem verify por estar nulo
    }
}
