package puc.airtrack.airtrack.logs;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import puc.airtrack.airtrack.Cliente.ClienteController;
import puc.airtrack.airtrack.Cliente.ClienteDTO;
import puc.airtrack.airtrack.Fornecedor.FornecedorController;
import puc.airtrack.airtrack.Fornecedor.FornecedorDTO;
import puc.airtrack.airtrack.Motor.Motor;
import puc.airtrack.airtrack.Motor.MotorController;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdem;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemController;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemController;
import puc.airtrack.airtrack.Pecas.Pecas;
import puc.airtrack.airtrack.Pecas.PecasController;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.User.UserController;

/**
 * Aspecto para registro automático de logs em operações de controladores
 */
@Aspect
@Component
public class LoggingAspect {

    @Autowired
    private LoggingService loggingService;

    /**
     * Intercepta e loga operações de criação/atualização em ClienteController
     */
    @AfterReturning(
            pointcut = "execution(* puc.airtrack.airtrack.Cliente.ClienteController.*(..)) && !execution(* puc.airtrack.airtrack.Cliente.ClienteController.getAll(..))",
            returning = "result")
    public void logClienteOperation(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Object[] args = joinPoint.getArgs();
        String operationType = determineOperationType(methodName);
        
        String clienteId = null;
        if (args.length > 0 && args[0] instanceof ClienteDTO) {
            clienteId = ((ClienteDTO) args[0]).getCpf();
        } else if (args.length > 0 && args[0] instanceof String) {
            clienteId = (String) args[0];
        }
        
        loggingService.logClienteOperation(
                ClienteController.class.getSimpleName() + "." + methodName,
                args.length > 0 ? args[0] : null,
                result,
                clienteId,
                operationType
        );
    }

    /**
     * Intercepta e loga operações de criação/atualização em FornecedorController
     */
    @AfterReturning(
            pointcut = "execution(* puc.airtrack.airtrack.Fornecedor.FornecedorController.*(..)) && !execution(* puc.airtrack.airtrack.Fornecedor.FornecedorController.getAll(..))",
            returning = "result")
    public void logFornecedorOperation(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Object[] args = joinPoint.getArgs();
        String operationType = determineOperationType(methodName);
        
        String fornecedorId = null;
        if (args.length > 0 && args[0] instanceof FornecedorDTO) {
            fornecedorId = ((FornecedorDTO) args[0]).getId();
        } else if (args.length > 0 && args[0] instanceof String) {
            fornecedorId = (String) args[0];
        }
        
        loggingService.logFornecedorOperation(
                FornecedorController.class.getSimpleName() + "." + methodName,
                args.length > 0 ? args[0] : null,
                result,
                fornecedorId,
                operationType
        );
    }

    /**
     * Intercepta e loga operações em CabecalhoOrdemController
     */
    @AfterReturning(
            pointcut = "execution(* puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemController.*(..)) && !execution(* puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemController.getAll(..))",
            returning = "result")
    public void logCabecalhoOrdemOperation(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Object[] args = joinPoint.getArgs();
        String operationType = determineOperationType(methodName);
        
        Integer ordemId = null;
        if (args.length > 0 && args[0] instanceof Integer) {
            ordemId = (Integer) args[0];
        } else if (result instanceof CabecalhoOrdem) {
            ordemId = ((CabecalhoOrdem) result).getId();
        }
        
        loggingService.logOrdemServicoOperation(
                CabecalhoOrdemController.class.getSimpleName() + "." + methodName,
                args.length > 0 ? args[0] : null,
                result,
                ordemId,
                operationType
        );
    }

    /**
     * Intercepta e loga operações em LinhaOrdemController
     */
    @AfterReturning(
            pointcut = "execution(* puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemController.*(..)) && !execution(* puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemController.getAll(..))",
            returning = "result")
    public void logLinhaOrdemOperation(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Object[] args = joinPoint.getArgs();
        String operationType = determineOperationType(methodName);
        
        Integer ordemId = null;
        if (args.length > 0 && args[0] instanceof Integer) {
            ordemId = (Integer) args[0];
        }
        
        loggingService.logOrdemServicoOperation(
                LinhaOrdemController.class.getSimpleName() + "." + methodName,
                args.length > 0 ? args[0] : null,
                result,
                ordemId,
                operationType
        );
    }

    /**
     * Intercepta e loga operações em PecasController
     */
    @AfterReturning(
            pointcut = "execution(* puc.airtrack.airtrack.Pecas.PecasController.*(..)) && !execution(* puc.airtrack.airtrack.Pecas.PecasController.getAll(..))",
            returning = "result")
    public void logPecaOperation(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Object[] args = joinPoint.getArgs();
        String operationType = determineOperationType(methodName);
        
        String pecaId = null;
        if (args.length > 0 && args[0] instanceof String) {
            pecaId = (String) args[0];
        } else if (result instanceof Pecas) {
            pecaId = String.valueOf(((Pecas) result).getId());
        }
        
        loggingService.logPecasOperation(
                PecasController.class.getSimpleName() + "." + methodName,
                args.length > 0 ? args[0] : null,
                result,
                pecaId,
                operationType
        );
    }

    /**
     * Intercepta e loga operações em MotorController
     */
    @AfterReturning(
            pointcut = "execution(* puc.airtrack.airtrack.Motor.MotorController.*(..)) && !execution(* puc.airtrack.airtrack.Motor.MotorController.getAll(..))",
            returning = "result")
    public void logMotorOperation(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Object[] args = joinPoint.getArgs();
        String operationType = determineOperationType(methodName);
        
        String motorId = null;
        if (args.length > 0 && args[0] instanceof String) {
            motorId = (String) args[0];
        } else if (result instanceof Motor) {
            motorId = ((Motor) result).getSerie_motor();
        }
        
        loggingService.logMotorOperation(
                MotorController.class.getSimpleName() + "." + methodName,
                args.length > 0 ? args[0] : null,
                result,
                motorId,
                operationType
        );
    }

    /**
     * Intercepta e loga operações em UserController
     */
    @AfterReturning(
            pointcut = "execution(* puc.airtrack.airtrack.User.UserController.*(..)) && !execution(* puc.airtrack.airtrack.User.UserController.getAll(..))",
            returning = "result")
    public void logUserOperation(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Object[] args = joinPoint.getArgs();
        String operationType = determineOperationType(methodName);
        
        String userId = null;
        if (args.length > 0 && args[0] instanceof String) {
            userId = (String) args[0];
        } else if (result instanceof User) {
            userId = ((User) result).getUsername();
        }
        
        loggingService.logUserOperation(
                UserController.class.getSimpleName() + "." + methodName,
                args.length > 0 ? args[0] : null,
                result,
                userId,
                operationType
        );
    }

    /**
     * Determina o tipo de operação com base no nome do método
     */
    private String determineOperationType(String methodName) {
        methodName = methodName.toLowerCase();
        if (methodName.startsWith("create") || methodName.startsWith("add") || methodName.startsWith("save")) {
            return "CREATE";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit") || methodName.startsWith("change")) {
            return "UPDATE";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        } else if (methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("search")) {
            return "READ";
        } else {
            return "OTHER";
        }
    }
}
