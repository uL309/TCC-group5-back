package puc.airtrack.airtrack.Services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import puc.airtrack.airtrack.logs.ClienteLogEntry;
import puc.airtrack.airtrack.logs.ClienteLogRepository;
import puc.airtrack.airtrack.logs.FornecedorLogEntry;
import puc.airtrack.airtrack.logs.FornecedorLogRepository;
import puc.airtrack.airtrack.logs.LoggingService;
import puc.airtrack.airtrack.logs.MotorLogEntry;
import puc.airtrack.airtrack.logs.MotorLogRepository;
import puc.airtrack.airtrack.logs.OrdemServicoLogEntry;
import puc.airtrack.airtrack.logs.OrdemServicoLogRepository;
import puc.airtrack.airtrack.logs.PecasLogEntry;
import puc.airtrack.airtrack.logs.PecasLogRepository;
import puc.airtrack.airtrack.logs.UserLogEntry;
import puc.airtrack.airtrack.logs.UserLogRepository;

@ExtendWith(MockitoExtension.class)
public class LoggingServiceTeste {

    @InjectMocks
    private LoggingService loggingService;

    @Mock
    private ClienteLogRepository clienteLogRepository;
    @Mock
    private FornecedorLogRepository fornecedorLogRepository;
    @Mock
    private OrdemServicoLogRepository ordemServicoLogRepository;
    @Mock
    private PecasLogRepository pecasLogRepository;
    @Mock
    private MotorLogRepository motorLogRepository;
    @Mock
    private UserLogRepository userLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    private SecurityContext originalContext;

    @BeforeEach
    void before() {
        originalContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    void after() {
        SecurityContextHolder.setContext(originalContext);
    }

    @Test
    void objectToJson_null_returnsNull() {
        Object result = ReflectionTestUtils.invokeMethod(loggingService, "objectToJson", (Object) null);
        assertNull(result);
    }

    @Test
    void objectToJson_truncatesLongJson() throws Exception {
        // cria string longa > 16000
        String longJson = "x".repeat(17000);
        when(objectMapper.writeValueAsString(any())).thenReturn(longJson);

        String out = (String) ReflectionTestUtils.invokeMethod(loggingService, "objectToJson", Map.of("k", "v"));
        assertNotNull(out);
        assertTrue(out.endsWith("... (truncado)"));
        assertTrue(out.length() <= 16000 + "... (truncado)".length());
    }

    @Test
    void objectToJson_handlesJsonProcessingException() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("fail") {});
        String out = (String) ReflectionTestUtils.invokeMethod(loggingService, "objectToJson", Map.of("k", "v"));
        assertNotNull(out);
        assertTrue(out.contains("Error converting to JSON"));
    }

    @Test
    void objectToJson_shortJson_returnsSame() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":1}");
        String out = (String) ReflectionTestUtils.invokeMethod(loggingService, "objectToJson", Map.of("k", "v"));
        assertEquals("{\"ok\":1}", out);
    }

    @Test
    void objectToJson_genericException_returnsProcessingMessage() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("boom"));
        String out = (String) ReflectionTestUtils.invokeMethod(loggingService, "objectToJson", Map.of("k", "v"));
        assertNotNull(out);
        assertTrue(out.contains("Error processing object"));
    }

    @Test
    void getCurrentUsername_authenticated_returnsName() {
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("tester");
        when(auth.getPrincipal()).thenReturn("principalObject");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        String name = (String) ReflectionTestUtils.invokeMethod(loggingService, "getCurrentUsername");
        assertEquals("tester", name);
    }

    @Test
    void getCurrentUsername_anonymousOrNotAuthenticated_returnsNull() {
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("anonymousUser");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        String name = (String) ReflectionTestUtils.invokeMethod(loggingService, "getCurrentUsername");
        assertNull(name);

        // not authenticated
        Authentication auth2 = mock(Authentication.class);
        when(auth2.isAuthenticated()).thenReturn(false);
        SecurityContext ctx2 = mock(SecurityContext.class);
        when(ctx2.getAuthentication()).thenReturn(auth2);
        SecurityContextHolder.setContext(ctx2);

        String name2 = (String) ReflectionTestUtils.invokeMethod(loggingService, "getCurrentUsername");
        assertNull(name2);
    }

    @Test
    void logClienteOperation_usesObjectMapper_and_savesEntity() throws Exception {
        // arrange
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":1}");
        when(clienteLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("userA");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        Object req = Map.of("r", 1);
        Object resp = Map.of("s", 2);

        Object saved = loggingService.logClienteOperation("Controller#method", req, resp, "cliente-123", "CREATE");

        verify(objectMapper, times(2)).writeValueAsString(any());
        verify(clienteLogRepository).save(any());
        assertNotNull(saved);
    }

    @Test
    void getClienteLogs_delegatesToRepository() {
        Pageable pageable = Pageable.ofSize(10);
        Page<Object> page = new PageImpl<>(List.of(mock(ClienteLogEntry.class)));
        when(clienteLogRepository.findAll(pageable)).thenReturn((Page) page);

        Page result = loggingService.getClienteLogs(pageable);
        assertSame(page, result);
    }

    // adicionar um teste simples para outro repositório/delegação como exemplo
    @Test
    void getOrdemServicoLogsByOrdemId_delegatesToRepository() {
        Pageable pageable = Pageable.ofSize(5);
        Page<OrdemServicoLogEntry> page = new PageImpl<>(List.of(mock(OrdemServicoLogEntry.class)));
        when(ordemServicoLogRepository.findByOrdemId(123, pageable)).thenReturn(page);

        Page<OrdemServicoLogEntry> result = loggingService.getOrdemServicoLogsByOrdemId(123, pageable);
        assertSame(page, result);
    }

    @Test
    void logFornecedorOperation_savesAndUsesObjectMapper() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"f\":1}");
        when(fornecedorLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("userF");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        Object saved = loggingService.logFornecedorOperation("C#m", Map.of("r",1), Map.of("s",2), "forn-1", "UPDATE");

        verify(objectMapper, times(2)).writeValueAsString(any());
        verify(fornecedorLogRepository).save(any());
        assertNotNull(saved);
    }

    @Test
    void logOrdemServicoOperation_and_getOrdemServicoLogs_delegation() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"o\":1}");
        when(ordemServicoLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("userO");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        Object saved = loggingService.logOrdemServicoOperation("C#m", Map.of(), Map.of(), 123, "CREATE");
        verify(ordemServicoLogRepository).save(any());
        assertNotNull(saved);

        Pageable p = Pageable.ofSize(1);
        Page<OrdemServicoLogEntry> page = new PageImpl<>(List.of(mock(OrdemServicoLogEntry.class)));
        when(ordemServicoLogRepository.findAll(p)).thenReturn((Page) page);
        Page<?> res = loggingService.getOrdemServicoLogs(p);
        assertSame(page, res);
    }

    @Test
    void logPecasOperation_and_getPecasLogs_delegation() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"p\":1}");
        when(pecasLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("userP");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        Object saved = loggingService.logPecasOperation("C#m", Map.of(), Map.of(), "peca-9", "DELETE");
        verify(pecasLogRepository).save(any());
        assertNotNull(saved);

        Pageable p = Pageable.ofSize(2);
        Page<PecasLogEntry> page = new PageImpl<>(List.of(mock(PecasLogEntry.class)));
        when(pecasLogRepository.findAll(p)).thenReturn((Page) page);
        Page<?> res = loggingService.getPecasLogs(p);
        assertSame(page, res);
    }

    @Test
    void logMotorOperation_and_getMotorLogs_delegation() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"m\":1}");
        when(motorLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("userM");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        Object saved = loggingService.logMotorOperation("C#m", Map.of(), Map.of(), "motor-7", "PATCH");
        verify(motorLogRepository).save(any());
        assertNotNull(saved);

        Pageable p = Pageable.ofSize(3);
        Page<MotorLogEntry> page = new PageImpl<>(List.of(mock(MotorLogEntry.class)));
        when(motorLogRepository.findAll(p)).thenReturn((Page) page);
        Page<?> res = loggingService.getMotorLogs(p);
        assertSame(page, res);
    }

    @Test
    void logUserOperation_and_getUserLogs_delegation() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"u\":1}");
        when(userLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("userU");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        Object saved = loggingService.logUserOperation("C#m", Map.of(), Map.of(), "target-5", "VIEW");
        verify(userLogRepository).save(any());
        assertNotNull(saved);

        Pageable p = Pageable.ofSize(4);
        Page<UserLogEntry> page = new PageImpl<>(List.of(mock(UserLogEntry.class)));
        when(userLogRepository.findAll(p)).thenReturn((Page) page);
        Page<?> res = loggingService.getUserLogs(p);
        assertSame(page, res);
    }

    @Test
    void getVariousByIdOrKey_delegatesCorrectly() {
        Pageable p = Pageable.ofSize(1);

        Page<ClienteLogEntry> cpage = new PageImpl<>(List.of(mock(ClienteLogEntry.class)));
        when(clienteLogRepository.findByClienteId("cliente-123", p)).thenReturn(cpage);
        Page<?> rc = loggingService.getClienteLogsByClienteId("cliente-123", p);
        assertSame(cpage, rc);

        Page<FornecedorLogEntry> fpage = new PageImpl<>(List.of(mock(FornecedorLogEntry.class)));
        when(fornecedorLogRepository.findByFornecedorId("forn-1", p)).thenReturn(fpage);
        assertSame(fpage, loggingService.getFornecedorLogsByFornecedorId("forn-1", p));

        Page<PecasLogEntry> ppage = new PageImpl<>(List.of(mock(PecasLogEntry.class)));
        when(pecasLogRepository.findByPecaId("peca-9", p)).thenReturn(ppage);
        assertSame(ppage, loggingService.getPecasLogsByPecaId("peca-9", p));

        Page<MotorLogEntry> mpage = new PageImpl<>(List.of(mock(MotorLogEntry.class)));
        when(motorLogRepository.findByMotorId("motor-7", p)).thenReturn(mpage);
        assertSame(mpage, loggingService.getMotorLogsByMotorId("motor-7", p));

        Page<UserLogEntry> upage = new PageImpl<>(List.of(mock(UserLogEntry.class)));
        when(userLogRepository.findByTargetUserId("target-5", p)).thenReturn(upage);
        assertSame(upage, loggingService.getUserLogsByTargetUserId("target-5", p));
    }
}