package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import puc.airtrack.airtrack.User.UserController;
import puc.airtrack.airtrack.Cliente.ClienteController;
import puc.airtrack.airtrack.Cliente.ClienteDTO;
import puc.airtrack.airtrack.Fornecedor.FornecedorController;
import puc.airtrack.airtrack.Motor.Motor;
import puc.airtrack.airtrack.Motor.MotorController;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdem;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemController;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemController;
import puc.airtrack.airtrack.Pecas.Pecas;
import puc.airtrack.airtrack.Pecas.PecasController;
import puc.airtrack.airtrack.logs.LoggingAspect;
import puc.airtrack.airtrack.logs.LoggingService;

@ExtendWith(MockitoExtension.class)
public class LoggingAspectTest {

    @InjectMocks
    private LoggingAspect aspect;

    @Mock
    private LoggingService loggingService;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Test
    void logClienteOperation_withClienteDTO_callsLoggingServiceCreate() throws Exception {
        // arrange
        ClienteDTO dto = mock(ClienteDTO.class);
        when(dto.getCpf()).thenReturn("cpf-123");
        Method m = this.getClass().getMethod("createClient", ClienteDTO.class);
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{dto});

        Object result = new Object();

        // act
        aspect.logClienteOperation(joinPoint, result);

        // assert
        verify(loggingService).logClienteOperation(
            eq(ClienteController.class.getSimpleName() + "." + m.getName()),
            same(dto),
            same(result),
            eq("cpf-123"),
            eq("CREATE")
        );
    }

    @Test
    void logFornecedorOperation_withStringArg_callsLoggingServiceUpdate() throws Exception {
        // arrange
        String fornId = "forn-9";
        Method m = this.getClass().getMethod("updateFornecedor", String.class);
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{fornId});

        Object result = new Object();

        // act
        aspect.logFornecedorOperation(joinPoint, result);

        // assert
        verify(loggingService).logFornecedorOperation(
            eq(FornecedorController.class.getSimpleName() + "." + m.getName()),
            eq(fornId),
            same(result),
            eq(fornId),
            eq("UPDATE")
        );
    }

    @Test
    void logCabecalhoOrdemOperation_withIntegerArg_usesArgAsId() throws Exception {
        // arrange
        Integer ordemId = 55;
        Method m = this.getClass().getMethod("getCabecalhoById", Integer.class);
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{ordemId});

        Object result = new Object();

        // act
        aspect.logCabecalhoOrdemOperation(joinPoint, result);

        // assert
        verify(loggingService).logOrdemServicoOperation(
            eq(CabecalhoOrdemController.class.getSimpleName() + "." + m.getName()),
            eq(ordemId),
            same(result),
            eq(ordemId),
            eq("READ")
        );
    }

    @Test
    void logCabecalhoOrdemOperation_withResultInstance_usesResultId() throws Exception {
        // arrange: args none, result is CabecalhoOrdem
        Method m = this.getClass().getMethod("otherCabecalhoMethod");
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        CabecalhoOrdem c = new CabecalhoOrdem();
        c.setId(777);

        // act
        aspect.logCabecalhoOrdemOperation(joinPoint, c);

        // assert
        verify(loggingService).logOrdemServicoOperation(
            eq(CabecalhoOrdemController.class.getSimpleName() + "." + m.getName()),
            isNull(),
            same(c),
            eq(777),
            eq("OTHER")
        );
    }

    @Test
    void logPecaOperation_withResultPecas_extractsIdAndType() throws Exception {
        // arrange
        Method m = this.getClass().getMethod("somePecaMethod");
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        Pecas p = new Pecas();
        p.setId(99);

        // act
        aspect.logPecaOperation(joinPoint, p);

        // assert
        verify(loggingService).logPecasOperation(
            eq(PecasController.class.getSimpleName() + "." + m.getName()),
            isNull(),
            same(p),
            eq("99"),
            eq("OTHER")
        );
    }

    @Test
    void logMotorOperation_withResultMotor_extractsSerie() throws Exception {
        // arrange
        Method m = this.getClass().getMethod("someMotorMethod");
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        Motor mo = new Motor();
        mo.setSerie_motor("SER-01");

        // act
        aspect.logMotorOperation(joinPoint, mo);

        // assert
        verify(loggingService).logMotorOperation(
            eq(MotorController.class.getSimpleName() + "." + m.getName()),
            isNull(),
            same(mo),
            eq("SER-01"),
            eq("OTHER")
        );
    }

    @Test
    void logUserOperation_withResultUser_extractsUsername() throws Exception {
        // arrange
        Method m = this.getClass().getMethod("someUserMethod");
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        puc.airtrack.airtrack.Login.User u = new puc.airtrack.airtrack.Login.User();
        u.setUsername("userX");

        // act
        aspect.logUserOperation(joinPoint, u);

        // assert
        verify(loggingService).logUserOperation(
            eq(UserController.class.getSimpleName() + "." + m.getName()),
            isNull(),
            same(u),
            eq("userX"),
            eq("OTHER")
        );
    }

    @Test
    void logLinhaOrdemOperation_withIntegerArg_callsLoggingService() throws Exception {
        Integer ordemId = 99;
        Method m = this.getClass().getMethod("getLinhaById", Integer.class);
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{ordemId});

        Object result = new Object();

        aspect.logLinhaOrdemOperation(joinPoint, result);

        verify(loggingService).logOrdemServicoOperation(
            eq(LinhaOrdemController.class.getSimpleName() + "." + m.getName()),
            eq(ordemId),
            same(result),
            eq(ordemId),
            eq("READ")
        );
    }

    @Test
    void logClienteOperation_withStringArg_callsLoggingService() throws Exception {
        String cpf = "cpf-999";
        Method m = this.getClass().getMethod("updateCliente", String.class);
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{cpf});

        Object result = new Object();

        aspect.logClienteOperation(joinPoint, result);

        verify(loggingService).logClienteOperation(
            eq(ClienteController.class.getSimpleName() + "." + m.getName()),
            eq(cpf),
            same(result),
            eq(cpf),
            eq("UPDATE")
        );
    }

    @Test
    void logFornecedorOperation_withDTO_callsLoggingServiceCreate() throws Exception {
        puc.airtrack.airtrack.Fornecedor.FornecedorDTO dto = mock(puc.airtrack.airtrack.Fornecedor.FornecedorDTO.class);
        when(dto.getId()).thenReturn("forn-42");
        Method m = this.getClass().getMethod("createFornecedor", puc.airtrack.airtrack.Fornecedor.FornecedorDTO.class);
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{dto});

        Object result = new Object();

        aspect.logFornecedorOperation(joinPoint, result);

        verify(loggingService).logFornecedorOperation(
            eq(FornecedorController.class.getSimpleName() + "." + m.getName()),
            same(dto),
            same(result),
            eq("forn-42"),
            eq("CREATE")
        );
    }

    @Test
    void logPecaOperation_withStringArg_callsLoggingServiceDelete() throws Exception {
        String pecaId = "55";
        Method m = this.getClass().getMethod("deletePeca", String.class);
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{pecaId});

        Object result = new Object();

        aspect.logPecaOperation(joinPoint, result);

        verify(loggingService).logPecasOperation(
            eq(PecasController.class.getSimpleName() + "." + m.getName()),
            eq(pecaId),
            same(result),
            eq(pecaId),
            eq("DELETE")
        );
    }

    @Test
    void determineOperationType_variousPrefixes_workAsExpected() {
        assertEquals("CREATE", org.springframework.test.util.ReflectionTestUtils.invokeMethod(aspect, "determineOperationType", "createSomething"));
        assertEquals("CREATE", org.springframework.test.util.ReflectionTestUtils.invokeMethod(aspect, "determineOperationType", "addSomething"));
        assertEquals("UPDATE", org.springframework.test.util.ReflectionTestUtils.invokeMethod(aspect, "determineOperationType", "updateSomething"));
        assertEquals("DELETE", org.springframework.test.util.ReflectionTestUtils.invokeMethod(aspect, "determineOperationType", "deleteSomething"));
        assertEquals("READ",   org.springframework.test.util.ReflectionTestUtils.invokeMethod(aspect, "determineOperationType", "getSomething"));
        assertEquals("OTHER",  org.springframework.test.util.ReflectionTestUtils.invokeMethod(aspect, "determineOperationType", "calculateSomething"));
    }

    // helper dummy methods referenced by reflection above
    public void createClient(ClienteDTO dto) {}
    public void updateFornecedor(String id) {}
    public void getCabecalhoById(Integer id) {}
    public void otherCabecalhoMethod() {}
    public void somePecaMethod() {}
    public void someMotorMethod() {}
    public void someUserMethod() {}
    public void getLinhaById(Integer id) {}
    public void updateCliente(String id) {}
    public void createFornecedor(puc.airtrack.airtrack.Fornecedor.FornecedorDTO dto) {}
    public void deletePeca(String id) {}

    @Test
    void logMotorOperation_withStringArg_resultNull() throws Exception {
        String serie = "SER-STR";
        Method m = this.getClass().getMethod("getMotorBySerie", String.class);
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{serie});

        aspect.logMotorOperation(joinPoint, null);

        verify(loggingService).logMotorOperation(
            eq(MotorController.class.getSimpleName() + "." + m.getName()),
            eq(serie),
            isNull(),
            eq(serie),
            eq("READ")
        );
    }

    @Test
    void logMotorOperation_withArgMotor_andResultMotor() throws Exception {
        Motor argMotor = new Motor();
        argMotor.setSerie_motor("SER-ARG");
        Motor resMotor = new Motor();
        resMotor.setSerie_motor("SER-RES");
        Method m = this.getClass().getMethod("updateMotor", Motor.class);
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{argMotor});

        aspect.logMotorOperation(joinPoint, resMotor);

        // dependendo da implementação, ajuste expectedSerie para SER-ARG ou SER-RES
        String expectedSerie = "SER-RES"; // aspecto usa série do resultado quando presente
        verify(loggingService).logMotorOperation(
            eq(MotorController.class.getSimpleName() + "." + m.getName()),
            same(argMotor),
            same(resMotor),
            eq(expectedSerie),
            eq("UPDATE")
        );
    }

    @Test
    void logMotorOperation_resultNull_noArgs() throws Exception {
        Method m = this.getClass().getMethod("otherMotorMethod");
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        aspect.logMotorOperation(joinPoint, null);

        verify(loggingService).logMotorOperation(
            eq(MotorController.class.getSimpleName() + "." + m.getName()),
            isNull(),
            isNull(),
            isNull(),
            eq("OTHER")
        );
    }

    @Test
    void logUserOperation_withStringArg_resultNull() throws Exception {
        String username = "userA";
        Method m = this.getClass().getMethod("getUserByUsername", String.class);
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{username});

        aspect.logUserOperation(joinPoint, null);

        verify(loggingService).logUserOperation(
            eq(UserController.class.getSimpleName() + "." + m.getName()),
            eq(username),
            isNull(),
            eq(username),
            eq("READ")
        );
    }

    @Test
    void logUserOperation_withArgUser_andResultUser() throws Exception {
        puc.airtrack.airtrack.Login.User arg = new puc.airtrack.airtrack.Login.User();
        arg.setUsername("argUser");
        puc.airtrack.airtrack.Login.User res = new puc.airtrack.airtrack.Login.User();
        res.setUsername("resUser");
        Method m = this.getClass().getMethod("updateUser", puc.airtrack.airtrack.Login.User.class);
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{arg});

        aspect.logUserOperation(joinPoint, res);

        String expected = "resUser"; // aspecto usa username do resultado quando presente
        verify(loggingService).logUserOperation(
            eq(UserController.class.getSimpleName() + "." + m.getName()),
            same(arg),
            same(res),
            eq(expected),
            eq("UPDATE")
        );
    }

    @Test
    void logUserOperation_argUserSemUsername_resultNull() throws Exception {
        puc.airtrack.airtrack.Login.User arg = new puc.airtrack.airtrack.Login.User(); // username null
        Method m = this.getClass().getMethod("deleteUser", puc.airtrack.airtrack.Login.User.class);
        when(methodSignature.getMethod()).thenReturn(m);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{arg});

        aspect.logUserOperation(joinPoint, null);

        verify(loggingService).logUserOperation(
            eq(UserController.class.getSimpleName() + "." + m.getName()),
            same(arg),
            isNull(),
            isNull(),
            eq("DELETE")
        );
    }

    // helper dummy methods for new tests
    public void getMotorBySerie(String serie) {}
    public void updateMotor(Motor m) {}
    public void otherMotorMethod() {}
    public void getUserByUsername(String u) {}
    public void updateUser(puc.airtrack.airtrack.Login.User u) {}
    public void deleteUser(puc.airtrack.airtrack.Login.User u) {}
}
