package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import puc.airtrack.airtrack.logs.ClienteLogEntry;
import puc.airtrack.airtrack.logs.ClienteLogRepository;
import puc.airtrack.airtrack.logs.FornecedorLogEntry;
import puc.airtrack.airtrack.logs.FornecedorLogRepository;
import puc.airtrack.airtrack.logs.LoggingService;
import puc.airtrack.airtrack.logs.LogsController;
import puc.airtrack.airtrack.logs.MotorLogEntry;
import puc.airtrack.airtrack.logs.MotorLogRepository;
import puc.airtrack.airtrack.logs.OrdemServicoLogEntry;
import puc.airtrack.airtrack.logs.OrdemServicoLogRepository;
import puc.airtrack.airtrack.logs.PecasLogEntry;
import puc.airtrack.airtrack.logs.PecasLogRepository;
import puc.airtrack.airtrack.logs.UserLogEntry;
import puc.airtrack.airtrack.logs.UserLogRepository;

@ExtendWith(SpringExtension.class)
@WebMvcTest(LogsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LogsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoggingService loggingService;

    @MockBean
    private ClienteLogRepository clienteLogRepository;
    @MockBean
    private FornecedorLogRepository fornecedorLogRepository;
    @MockBean
    private OrdemServicoLogRepository ordemServicoLogRepository;
    @MockBean
    private PecasLogRepository pecasLogRepository;
    @MockBean
    private MotorLogRepository motorLogRepository;
    @MockBean
    private UserLogRepository userLogRepository;

    @MockBean
    private puc.airtrack.airtrack.TokenService tokenService;

    @MockBean
    private puc.airtrack.airtrack.SecurityFilter securityFilter;

    @MockBean
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void getClienteLogs_byClienteId_returnsOk() throws Exception {
        Page<ClienteLogEntry> page = new PageImpl<>(List.of(mock(ClienteLogEntry.class)));
        when(clienteLogRepository.findByClienteId(eq("cliente-1"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/clientes").param("clienteId", "cliente-1"))
               .andExpect(status().isOk());

        verify(clienteLogRepository).findByClienteId(eq("cliente-1"), any());
    }

    @Test
    void getClienteLogs_byUsername_returnsOk() throws Exception {
        Page<ClienteLogEntry> page = new PageImpl<>(List.of(mock(ClienteLogEntry.class)));
        when(clienteLogRepository.findByUsername(eq("userA"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/clientes").param("username", "userA"))
               .andExpect(status().isOk());

        verify(clienteLogRepository).findByUsername(eq("userA"), any());
    }

    @Test
    void getClienteLogs_byDateRange_returnsOk() throws Exception {
        Page<ClienteLogEntry> page = new PageImpl<>(List.of(mock(ClienteLogEntry.class)));
        when(clienteLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any())).thenReturn(page);

        String start = LocalDateTime.now().minusDays(1).toString();
        String end = LocalDateTime.now().toString();

        mockMvc.perform(get("/api/logs/clientes")
                .param("startDate", start)
                .param("endDate", end))
               .andExpect(status().isOk());

        verify(clienteLogRepository).findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any());
    }

    @Test
    void getFornecedorLogs_byFornecedorId_returnsOk() throws Exception {
        Page<FornecedorLogEntry> page = new PageImpl<>(List.of(mock(FornecedorLogEntry.class)));
        when(fornecedorLogRepository.findByFornecedorId(eq("forn-1"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/fornecedores").param("fornecedorId", "forn-1"))
               .andExpect(status().isOk());

        verify(fornecedorLogRepository).findByFornecedorId(eq("forn-1"), any());
    }

    @Test
    void getOrdemServicoLogs_byOrdemId_returnsOk() throws Exception {
        Page<OrdemServicoLogEntry> page = new PageImpl<>(List.of(mock(OrdemServicoLogEntry.class)));
        when(ordemServicoLogRepository.findByOrdemId(eq(123), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/ordens-servico").param("ordemId", "123"))
               .andExpect(status().isOk());

        verify(ordemServicoLogRepository).findByOrdemId(eq(123), any());
    }

    @Test
    void getPecasLogs_byPecaId_returnsOk() throws Exception {
        Page<PecasLogEntry> page = new PageImpl<>(List.of(mock(PecasLogEntry.class)));
        when(pecasLogRepository.findByPecaId(eq("peca-9"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/pecas").param("pecaId", "peca-9"))
               .andExpect(status().isOk());

        verify(pecasLogRepository).findByPecaId(eq("peca-9"), any());
    }

    @Test
    void getMotorLogs_byMotorId_returnsOk() throws Exception {
        Page<MotorLogEntry> page = new PageImpl<>(List.of(mock(MotorLogEntry.class)));
        when(motorLogRepository.findByMotorId(eq("motor-7"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/motores").param("motorId", "motor-7"))
               .andExpect(status().isOk());

        verify(motorLogRepository).findByMotorId(eq("motor-7"), any());
    }

    @Test
    void getUserLogs_byUserId_returnsOk() throws Exception {
        Page<UserLogEntry> page = new PageImpl<>(List.of(mock(UserLogEntry.class)));
        when(userLogRepository.findByTargetUserId(eq("target-5"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/logs/users").param("userId", "target-5"))
               .andExpect(status().isOk());

        verify(userLogRepository).findByTargetUserId(eq("target-5"), any());
    }

    @Test
    void getClienteLogs_noParams_returnsOk() throws Exception {
        Page<ClienteLogEntry> page = new PageImpl<>(List.of(mock(ClienteLogEntry.class)));
        // se o controller chama findAll(Pageable)
        when(clienteLogRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/logs/clientes"))
               .andExpect(status().isOk());

    }

    @Test
    void getClienteLogs_byOperationType_returnsOk() throws Exception {
        Page<ClienteLogEntry> page = new PageImpl<>(List.of(mock(ClienteLogEntry.class)));
        when(clienteLogRepository.findByOperationType(eq("CREATE"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/clientes").param("operationType", "CREATE"))
               .andExpect(status().isOk());

        verify(clienteLogRepository).findByOperationType(eq("CREATE"), any());
    }

    @Test
    void getFornecedorLogs_default_returnsOk() throws Exception {
        Page<FornecedorLogEntry> page = new PageImpl<>(List.of(mock(FornecedorLogEntry.class)));
        when(fornecedorLogRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/logs/fornecedores"))
               .andExpect(status().isOk());

        verify(fornecedorLogRepository).findAll(any(Pageable.class));
    }

    @Test
    void getOrdemServicoLogs_byUsername_returnsOk() throws Exception {
        Page<OrdemServicoLogEntry> page = new PageImpl<>(List.of(mock(OrdemServicoLogEntry.class)));
        when(ordemServicoLogRepository.findByUsername(eq("userX"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/ordens-servico").param("username", "userX"))
               .andExpect(status().isOk());

        verify(ordemServicoLogRepository).findByUsername(eq("userX"), any());
    }

    @Test
    void getPecasLogs_default_returnsOk() throws Exception {
        Page<PecasLogEntry> page = new PageImpl<>(List.of(mock(PecasLogEntry.class)));
        when(pecasLogRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/logs/pecas"))
               .andExpect(status().isOk());

        verify(pecasLogRepository).findAll(any(Pageable.class));
    }

    @Test
    void getMotorLogs_default_returnsOk() throws Exception {
        Page<MotorLogEntry> page = new PageImpl<>(List.of(mock(MotorLogEntry.class)));
        when(motorLogRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/logs/motores"))
               .andExpect(status().isOk());

        verify(motorLogRepository).findAll(any(Pageable.class));
    }

    @Test
    void getUserLogs_default_returnsOk() throws Exception {
        Page<UserLogEntry> page = new PageImpl<>(List.of(mock(UserLogEntry.class)));
        when(userLogRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/logs/users"))
               .andExpect(status().isOk());

        verify(userLogRepository).findAll(any(Pageable.class));
    }

    // testar parsing de datas (cobre DateTimeFormat)
    @Test
    void getClienteLogs_dateParsing_branch() throws Exception {
        Page<ClienteLogEntry> page = new PageImpl<>(List.of(mock(ClienteLogEntry.class)));
        when(clienteLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any())).thenReturn(page);

        String start = "2023-01-01T00:00:00";
        String end = "2023-01-02T00:00:00";

        mockMvc.perform(get("/api/logs/clientes").param("startDate", start).param("endDate", end))
               .andExpect(status().isOk());

        verify(clienteLogRepository).findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any());
    }
        @Test
    void getOrdemServicoLogs_byDateRange_returnsOk() throws Exception {
        Page<OrdemServicoLogEntry> page = new PageImpl<>(List.of(mock(OrdemServicoLogEntry.class)));
        when(ordemServicoLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any())).thenReturn(page);

        String start = LocalDateTime.now().minusDays(2).toString();
        String end = LocalDateTime.now().toString();

        mockMvc.perform(get("/api/logs/ordens-servico")
                .param("startDate", start)
                .param("endDate", end))
               .andExpect(status().isOk());

        verify(ordemServicoLogRepository).findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any());
    }

    @Test
    void getOrdemServicoLogs_byOperationType_returnsOk() throws Exception {
        Page<OrdemServicoLogEntry> page = new PageImpl<>(List.of(mock(OrdemServicoLogEntry.class)));
        when(ordemServicoLogRepository.findByOperationType(eq("DELETE"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/ordens-servico").param("operationType", "DELETE"))
               .andExpect(status().isOk());

        verify(ordemServicoLogRepository).findByOperationType(eq("DELETE"), any());
    }

    @Test
    void getOrdemServicoLogs_idOverridesOtherParams() throws Exception {
        Page<OrdemServicoLogEntry> page = new PageImpl<>(List.of(mock(OrdemServicoLogEntry.class)));
        when(ordemServicoLogRepository.findByOrdemId(eq(999), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/ordens-servico")
                .param("ordemId", "999")
                .param("username", "ignoredUser")
                .param("operationType", "CREATE"))
               .andExpect(status().isOk());

        verify(ordemServicoLogRepository).findByOrdemId(eq(999), any());
        verify(ordemServicoLogRepository, never()).findByUsername(eq("ignoredUser"), any());
        verify(ordemServicoLogRepository, never()).findByOperationType(eq("CREATE"), any());
    }

    @Test
    void getOrdemServicoLogs_paginationAndSort() throws Exception {
        Page<OrdemServicoLogEntry> page = new PageImpl<>(List.of(mock(OrdemServicoLogEntry.class)));
        when(ordemServicoLogRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/logs/ordens-servico")
                .param("page", "2")
                .param("size", "5")
                .param("orderBy", "timestamp")
                .param("direction", "DESC"))
               .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(ordemServicoLogRepository).findAll(captor.capture());
        Pageable p = captor.getValue();
        assertEquals(2, p.getPageNumber());
        assertEquals(5, p.getPageSize());
        Sort.Order order = p.getSort().getOrderFor("timestamp");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void getFornecedorLogs_byUsername_returnsOk() throws Exception {
        Page<FornecedorLogEntry> page = new PageImpl<>(List.of(mock(FornecedorLogEntry.class)));
        when(fornecedorLogRepository.findByUsername(eq("fornUser"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/fornecedores").param("username", "fornUser"))
               .andExpect(status().isOk());

        verify(fornecedorLogRepository).findByUsername(eq("fornUser"), any());
    }

    @Test
    void getFornecedorLogs_byOperationType_returnsOk() throws Exception {
        Page<FornecedorLogEntry> page = new PageImpl<>(List.of(mock(FornecedorLogEntry.class)));
        when(fornecedorLogRepository.findByOperationType(eq("UPDATE"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/fornecedores").param("operationType", "UPDATE"))
               .andExpect(status().isOk());

        verify(fornecedorLogRepository).findByOperationType(eq("UPDATE"), any());
    }

    @Test
    void getFornecedorLogs_byDateRange_returnsOk() throws Exception {
        Page<FornecedorLogEntry> page = new PageImpl<>(List.of(mock(FornecedorLogEntry.class)));
        when(fornecedorLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/fornecedores")
                .param("startDate", LocalDateTime.now().minusHours(5).toString())
                .param("endDate", LocalDateTime.now().toString()))
               .andExpect(status().isOk());

        verify(fornecedorLogRepository).findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any());
    }

    @Test
    void getPecasLogs_byOperationType_returnsOk() throws Exception {
        Page<PecasLogEntry> page = new PageImpl<>(List.of(mock(PecasLogEntry.class)));
        when(pecasLogRepository.findByOperationType(eq("CREATE"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/pecas").param("operationType", "CREATE"))
               .andExpect(status().isOk());

        verify(pecasLogRepository).findByOperationType(eq("CREATE"), any());
    }

    @Test
    void getPecasLogs_byDateRange_returnsOk() throws Exception {
        Page<PecasLogEntry> page = new PageImpl<>(List.of(mock(PecasLogEntry.class)));
        when(pecasLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/pecas")
                .param("startDate", LocalDateTime.now().minusDays(3).toString())
                .param("endDate", LocalDateTime.now().toString()))
               .andExpect(status().isOk());

        verify(pecasLogRepository).findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any());
    }

    @Test
    void getMotorLogs_byOperationType_returnsOk() throws Exception {
        Page<MotorLogEntry> page = new PageImpl<>(List.of(mock(MotorLogEntry.class)));
        when(motorLogRepository.findByOperationType(eq("DELETE"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/motores").param("operationType", "DELETE"))
               .andExpect(status().isOk());

        verify(motorLogRepository).findByOperationType(eq("DELETE"), any());
    }

    @Test
    void getMotorLogs_byDateRange_returnsOk() throws Exception {
        Page<MotorLogEntry> page = new PageImpl<>(List.of(mock(MotorLogEntry.class)));
        when(motorLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/motores")
                .param("startDate", LocalDateTime.now().minusHours(10).toString())
                .param("endDate", LocalDateTime.now().toString()))
               .andExpect(status().isOk());

        verify(motorLogRepository).findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any());
    }

    @Test
    void getUserLogs_byUsername_returnsOk() throws Exception {
        Page<UserLogEntry> page = new PageImpl<>(List.of(mock(UserLogEntry.class)));
        when(userLogRepository.findByUsername(eq("actorUser"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/users").param("username", "actorUser"))
               .andExpect(status().isOk());

        verify(userLogRepository).findByUsername(eq("actorUser"), any());
    }

    @Test
    void getUserLogs_byOperationType_returnsOk() throws Exception {
        Page<UserLogEntry> page = new PageImpl<>(List.of(mock(UserLogEntry.class)));
        when(userLogRepository.findByOperationType(eq("LOGIN"), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/users").param("operationType", "LOGIN"))
               .andExpect(status().isOk());

        verify(userLogRepository).findByOperationType(eq("LOGIN"), any());
    }

    @Test
    void getUserLogs_byDateRange_returnsOk() throws Exception {
        Page<UserLogEntry> page = new PageImpl<>(List.of(mock(UserLogEntry.class)));
        when(userLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any())).thenReturn(page);

        mockMvc.perform(get("/api/logs/users")
                .param("startDate", LocalDateTime.now().minusDays(4).toString())
                .param("endDate", LocalDateTime.now().toString()))
               .andExpect(status().isOk());

        verify(userLogRepository).findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any());
    }

    @Test
    void getOrdemServicoLogs_invalidDate_returnsBadRequestOrOk() throws Exception {
        mockMvc.perform(get("/api/logs/ordens-servico")
                .param("startDate", "INVALID")
                .param("endDate", "ALSO_INVALID"))
               .andExpect(status().is4xxClientError());
    }

    @Test
    void getFornecedorLogs_invalidDate_returnsClientError() throws Exception {
        mockMvc.perform(get("/api/logs/fornecedores")
                .param("startDate", "XXXX")
                .param("endDate", "YYYY"))
               .andExpect(status().is4xxClientError());
    }
}

