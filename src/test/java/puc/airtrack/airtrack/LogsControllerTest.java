package puc.airtrack.airtrack;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

        mockMvc.perform(get("/api/logs/ordens").param("ordemId", "123"))
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
        // se a chamada usar Example<UserLogEntry> faça:
        when(userLogRepository.findAll(ArgumentMatchers.<Example<UserLogEntry>>any(), any(Pageable.class)))
            .thenReturn(page);

        // ou, se o controller chama findByTargetUserId, mantenha o stub original:
        // when(userLogRepository.findByTargetUserId(eq("target-5"), any(Pageable.class))).thenReturn(page);

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

        mockMvc.perform(get("/api/logs/ordens").param("username", "userX"))
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
}

