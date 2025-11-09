package puc.airtrack.airtrack.OrdemDeServico;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.BlobClient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import puc.airtrack.airtrack.services.AzureBlobStorageService;
import puc.airtrack.airtrack.services.OrdemServicoPdfService;
import puc.airtrack.airtrack.services.AuthUtil;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Motor.Motor;
import puc.airtrack.airtrack.Motor.MotorRepository;
import puc.airtrack.airtrack.tipoMotor.TipoMotor;
import puc.airtrack.airtrack.tipoMotor.TipoMotorRepository;
import puc.airtrack.airtrack.Fornecedor.FornecedorRepo;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ordem")
@Tag(name = "Ordem de Serviço", description = "Gerenciamento completo de ordens de serviço de manutenção - criação, consulta, atualização e controle de status")
@SecurityRequirement(name = "bearerAuth")
public class CabecalhoOrdemController {
    @Autowired
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;
    @Autowired
    private LinhaOrdemService linhaOrdemService;
    @Autowired
    private CabecalhoOrdemService cabecalhoOrdemService;
    @Autowired
    private AzureBlobStorageService azureBlobStorageService;
    @Autowired
    private OrdemServicoPdfService ordemServicoPdfService;
    @Autowired
    private MotorRepository motorRepository;
    @Autowired
    private TipoMotorRepository tipoMotorRepository;
    @Autowired
    private FornecedorRepo fornecedorRepo;

    @Operation(
        summary = "Criar ordem de serviço",
        description = "Cria uma nova ordem de serviço de manutenção para um motor. A OS pode ser preventiva, corretiva ou overhaul.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da ordem de serviço",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CabecalhoOrdemDTO.class),
                examples = @ExampleObject(
                    name = "OS Preventiva",
                    value = """
                    {
                      "dataAbertura": "2025-10-19",
                      "descricao": "Revisão programada de 500 horas",
                      "tipo": "PREVENTIVA",
                      "tempoEstimado": 120.0,
                      "status": "PENDENTE",
                      "valorHora": 150.00,
                      "clienteId": "123.456.789-00",
                      "motorId": "1",
                      "supervisorId": "2",
                      "engenheiroAtuanteId": "3"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ordem criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping("/create")
    public ResponseEntity<String> createCabecalho(@RequestBody CabecalhoOrdemDTO dto) {
        return cabecalhoOrdemService.createCabecalho(dto);
    }

    @Operation(
        summary = "Atualizar ordem de serviço",
        description = "Atualiza os dados de uma ordem de serviço existente. Permite alterar status, tempo usado, engenheiro responsável, etc."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ordem atualizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Ordem não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @PutMapping("/update")
    public ResponseEntity<String> updateCabecalho(@RequestBody CabecalhoOrdemDTO dto) {
        return cabecalhoOrdemService.updateCabecalho(dto);
    }

    @Operation(
        summary = "Buscar ordem de serviço por ID",
        description = "Retorna todos os dados de uma ordem de serviço específica, incluindo linhas (peças utilizadas)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ordem encontrada"),
        @ApiResponse(responseCode = "404", description = "Ordem não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @GetMapping("/get")
    public ResponseEntity<CabecalhoOrdemDTO> getCabecalho(@RequestParam int id) {
        Optional<CabecalhoOrdem> opt = cabecalhoOrdemRepository.findById(id);
        if (opt.isPresent()) {
            CabecalhoOrdemDTO dto = convertToDTO(opt.get());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/list")
    public ResponseEntity<List<CabecalhoOrdemDTO>> getAllCabecalhos() {
        List<CabecalhoOrdem> list = cabecalhoOrdemRepository.findAllByOrderByIdDesc();
        List<CabecalhoOrdemDTO> dtos = new ArrayList<>();
        for (CabecalhoOrdem entity : list) {
            CabecalhoOrdemDTO dto = convertToDTO(entity);
            System.out.println("Adding CabecalhoOrdemDTO: " + dto);
            dtos.add(dto);
        }
        return ResponseEntity.ok(dtos);
    }

    @Operation(
        summary = "Buscar ordens de serviço do engenheiro logado",
        description = "Retorna todas as ordens de serviço atribuídas ao engenheiro logado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ordens encontradas"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas engenheiros podem acessar")
    })
    @GetMapping("/engenheiro/minhas-os")
    public ResponseEntity<List<CabecalhoOrdemDTO>> getMinhasOrdens() {
        User usuario = AuthUtil.getUsuarioLogado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        
        // A verificação de autorização já foi feita pelo SecurityConfig
        // Se chegou aqui, o usuário tem permissão (ROLE_ENGENHEIRO ou ROLE_ADMIN)
        // Quando ADMIN está em modo override, pode não ter OS atribuídas, mas pode acessar o endpoint
        List<CabecalhoOrdem> list = cabecalhoOrdemRepository.findByEngenheiroAtuanteOrderByIdDesc(usuario);
        List<CabecalhoOrdemDTO> dtos = new ArrayList<>();
        for (CabecalhoOrdem entity : list) {
            CabecalhoOrdemDTO dto = convertToDTO(entity);
            dtos.add(dto);
        }
        return ResponseEntity.ok(dtos);
    }

    @Operation(
        summary = "Buscar estatísticas do engenheiro logado",
        description = "Retorna estatísticas de trabalho do engenheiro logado, incluindo total de OS, tempo trabalhado, etc."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas encontradas"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas engenheiros podem acessar")
    })
    @GetMapping("/engenheiro/stats")
    public ResponseEntity<EngenheiroStatsDTO> getEstatisticas() {
        User usuario = AuthUtil.getUsuarioLogado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        
        // A verificação de autorização já foi feita pelo SecurityConfig
        // Se chegou aqui, o usuário tem permissão (ROLE_ENGENHEIRO ou ROLE_ADMIN)
        // Quando ADMIN está em modo override, pode não ter OS atribuídas, mas pode acessar o endpoint
        List<CabecalhoOrdem> todasOs = cabecalhoOrdemRepository.findByEngenheiroAtuanteOrderByIdDesc(usuario);
        List<CabecalhoOrdem> osAndamento = cabecalhoOrdemRepository.findByEngenheiroAtuanteAndStatusOrderByIdDesc(
            usuario, puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.ANDAMENTO);
        List<CabecalhoOrdem> osPendentes = cabecalhoOrdemRepository.findByEngenheiroAtuanteAndStatusOrderByIdDesc(
            usuario, puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.PENDENTE);
        List<CabecalhoOrdem> osConcluidas = cabecalhoOrdemRepository.findByEngenheiroAtuanteAndStatusOrderByIdDesc(
            usuario, puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.CONCLUIDA);
        
        EngenheiroStatsDTO stats = new EngenheiroStatsDTO();
        stats.setTotalOs(todasOs.size());
        stats.setOsEmAndamento(osAndamento.size());
        stats.setOsPendentes(osPendentes.size());
        stats.setOsConcluidas(osConcluidas.size());
        
        // Calcular tempo total trabalhado
        float tempoTotal = 0;
        for (CabecalhoOrdem os : todasOs) {
            tempoTotal += os.getTempoUsado();
        }
        stats.setTempoTotalTrabalhado(tempoTotal);
        
        // Calcular tempo trabalhado esta semana
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeek = now.get(weekFields.weekOfWeekBasedYear());
        int currentYear = now.get(weekFields.weekBasedYear());
        
        float tempoEstaSemana = 0;
        int completadasEsteMes = 0;
        LocalDate primeiroDiaMes = now.withDayOfMonth(1);
        
        for (CabecalhoOrdem os : todasOs) {
            if (os.getDataAbertura() != null && !os.getDataAbertura().isEmpty()) {
                try {
                    LocalDate dataAbertura = LocalDate.parse(os.getDataAbertura());
                    int semanaOs = dataAbertura.get(weekFields.weekOfWeekBasedYear());
                    int anoOs = dataAbertura.get(weekFields.weekBasedYear());
                    
                    if (semanaOs == currentWeek && anoOs == currentYear) {
                        tempoEstaSemana += os.getTempoUsado();
                    }
                    
                    if (os.getStatus() == puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.CONCLUIDA 
                        && dataAbertura.isAfter(primeiroDiaMes.minusDays(1))) {
                        completadasEsteMes++;
                    }
                } catch (Exception e) {
                    // Ignora erros de parsing de data
                }
            }
        }
        
        stats.setTempoTotalEstaSemana(tempoEstaSemana);
        stats.setOsCompletadasEsteMes(completadasEsteMes);
        
        return ResponseEntity.ok(stats);
    }

    @Operation(
        summary = "Buscar OS concluídas recentes para auditoria",
        description = "Retorna as últimas ordens de serviço concluídas ordenadas por ID descendente (mais recentes primeiro)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ordens encontradas"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas auditores podem acessar")
    })
    @GetMapping("/auditor/os-concluidas")
    public ResponseEntity<List<CabecalhoOrdemDTO>> getOsConcluidasRecentes(
            @RequestParam(defaultValue = "5") int limit) {
        List<CabecalhoOrdem> list = cabecalhoOrdemRepository
            .findByStatusOrderByIdDesc(OrdemStatus.CONCLUIDA);
        
        // Limitar a quantidade solicitada
        List<CabecalhoOrdem> limitedList = list.stream()
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
        
        List<CabecalhoOrdemDTO> dtos = new ArrayList<>();
        for (CabecalhoOrdem entity : limitedList) {
            CabecalhoOrdemDTO dto = convertToDTO(entity);
            dtos.add(dto);
        }
        return ResponseEntity.ok(dtos);
    }

    @Operation(
        summary = "Buscar estatísticas para auditoria",
        description = "Retorna estatísticas gerais do sistema para auditoria, incluindo total de OS, OS concluídas, etc."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas encontradas"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas auditores podem acessar")
    })
    @GetMapping("/auditor/stats")
    public ResponseEntity<AuditorStatsDTO> getEstatisticasAuditor() {
        List<CabecalhoOrdem> todasOs = cabecalhoOrdemRepository.findAllByOrderByIdDesc();
        List<CabecalhoOrdem> osConcluidas = cabecalhoOrdemRepository
            .findByStatusOrderByIdDesc(OrdemStatus.CONCLUIDA);
        List<CabecalhoOrdem> osAndamento = cabecalhoOrdemRepository
            .findByStatusOrderByIdDesc(OrdemStatus.ANDAMENTO);
        List<CabecalhoOrdem> osPendentes = cabecalhoOrdemRepository
            .findByStatusOrderByIdDesc(OrdemStatus.PENDENTE);
        
        AuditorStatsDTO stats = new AuditorStatsDTO();
        stats.setTotalOs(todasOs.size());
        stats.setOsConcluidas(osConcluidas.size());
        stats.setOsEmAndamento(osAndamento.size());
        stats.setOsPendentes(osPendentes.size());
        
        // Calcular OS concluídas este mês e esta semana
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeek = now.get(weekFields.weekOfWeekBasedYear());
        int currentYear = now.get(weekFields.weekBasedYear());
        LocalDate primeiroDiaMes = now.withDayOfMonth(1);
        
        int concluidasEsteMes = 0;
        int concluidasEstaSemana = 0;
        
        for (CabecalhoOrdem os : osConcluidas) {
            if (os.getDataFechamento() != null && !os.getDataFechamento().isEmpty()) {
                try {
                    LocalDate dataFechamento = LocalDate.parse(os.getDataFechamento());
                    
                    // Verificar se foi concluída este mês
                    if (dataFechamento.isAfter(primeiroDiaMes.minusDays(1))) {
                        concluidasEsteMes++;
                    }
                    
                    // Verificar se foi concluída esta semana
                    int semanaOs = dataFechamento.get(weekFields.weekOfWeekBasedYear());
                    int anoOs = dataFechamento.get(weekFields.weekBasedYear());
                    if (semanaOs == currentWeek && anoOs == currentYear) {
                        concluidasEstaSemana++;
                    }
                } catch (Exception e) {
                    // Ignora erros de parsing de data
                }
            }
        }
        
        stats.setOsConcluidasEsteMes(concluidasEsteMes);
        stats.setOsConcluidasEstaSemana(concluidasEstaSemana);
        
        // Calcular taxa de conclusão
        float taxaConclusao = 0;
        if (todasOs.size() > 0) {
            taxaConclusao = (float) osConcluidas.size() / todasOs.size() * 100;
        }
        stats.setTaxaConclusao(taxaConclusao);
        
        // Calcular motores com TBO expirado
        List<Motor> todosMotores = motorRepository.findAll();
        int motoresTboExpirado = 0;
        for (Motor motor : todosMotores) {
            if (motor.getStatus() != null && motor.getStatus()) {
                TipoMotor tipoMotor = tipoMotorRepository.findByMarcaAndModelo(motor.getMarca(), motor.getModelo());
                if (tipoMotor != null && tipoMotor.getTbo() > 0) {
                    float percentual = (float) motor.getHoras_operacao() / tipoMotor.getTbo() * 100;
                    if (percentual >= 100) {
                        motoresTboExpirado++;
                    }
                }
            }
        }
        stats.setMotoresTboExpirado(motoresTboExpirado);
        
        // Calcular OS pendentes críticas (há mais de 7 dias)
        LocalDate seteDiasAtras = now.minusDays(7);
        int osPendentesCriticas = 0;
        for (CabecalhoOrdem os : osPendentes) {
            if (os.getDataAbertura() != null && !os.getDataAbertura().isEmpty()) {
                try {
                    LocalDate dataAbertura = LocalDate.parse(os.getDataAbertura());
                    if (dataAbertura.isBefore(seteDiasAtras) || dataAbertura.isEqual(seteDiasAtras)) {
                        osPendentesCriticas++;
                    }
                } catch (Exception e) {
                    // Ignora erros de parsing
                }
            }
        }
        stats.setOsPendentesCriticas(osPendentesCriticas);
        
        // Calcular tempo médio de conclusão (últimos 30 dias)
        LocalDate trintaDiasAtras = now.minusDays(30);
        float tempoMedioConclusao = 0;
        int osCompletadasUltimos30Dias = 0;
        long totalDias = 0;
        
        for (CabecalhoOrdem os : osConcluidas) {
            if (os.getDataAbertura() != null && !os.getDataAbertura().isEmpty() 
                && os.getDataFechamento() != null && !os.getDataFechamento().isEmpty()) {
                try {
                    LocalDate dataAbertura = LocalDate.parse(os.getDataAbertura());
                    LocalDate dataFechamento = LocalDate.parse(os.getDataFechamento());
                    
                    // Apenas OS concluídas nos últimos 30 dias
                    if (dataFechamento.isAfter(trintaDiasAtras.minusDays(1))) {
                        long diasEntre = java.time.temporal.ChronoUnit.DAYS.between(dataAbertura, dataFechamento);
                        if (diasEntre >= 0) {
                            totalDias += diasEntre;
                            osCompletadasUltimos30Dias++;
                        }
                    }
                } catch (Exception e) {
                    // Ignora erros de parsing
                }
            }
        }
        
        if (osCompletadasUltimos30Dias > 0) {
            tempoMedioConclusao = (float) totalDias / osCompletadasUltimos30Dias;
        }
        stats.setTempoMedioConclusao(tempoMedioConclusao);
        
        return ResponseEntity.ok(stats);
    }

    @Operation(
        summary = "Buscar estatísticas para supervisor",
        description = "Retorna estatísticas gerais do sistema para supervisão, incluindo motores, TBO, OS e fornecedores."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas encontradas"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas supervisores podem acessar")
    })
    @GetMapping("/supervisor/stats")
    public ResponseEntity<SupervisorStatsDTO> getEstatisticasSupervisor() {
        // Buscar todos os motores
        List<Motor> todosMotores = motorRepository.findAll();
        
        // Calcular motores com TBO próximo (>= 80%) e expirado (> 100%)
        int motoresTboProximo = 0;
        int motoresTboExpirado = 0;
        
        for (Motor motor : todosMotores) {
            if (motor.getStatus() != null && motor.getStatus()) { // Apenas motores ativos
                TipoMotor tipoMotor = tipoMotorRepository.findByMarcaAndModelo(motor.getMarca(), motor.getModelo());
                if (tipoMotor != null && tipoMotor.getTbo() > 0) {
                    float percentual = (float) motor.getHoras_operacao() / tipoMotor.getTbo() * 100;
                    if (percentual >= 100) {
                        motoresTboExpirado++;
                    } else if (percentual >= 80) {
                        motoresTboProximo++;
                    }
                }
            }
        }
        
        // Buscar OS
        List<CabecalhoOrdem> osPendentes = cabecalhoOrdemRepository
            .findByStatusOrderByIdDesc(OrdemStatus.PENDENTE);
        List<CabecalhoOrdem> osAndamento = cabecalhoOrdemRepository
            .findByStatusOrderByIdDesc(OrdemStatus.ANDAMENTO);
        
        // Calcular OS concluídas este mês
        LocalDate now = LocalDate.now();
        LocalDate primeiroDiaMes = now.withDayOfMonth(1);
        int osConcluidasMes = 0;
        
        List<CabecalhoOrdem> osConcluidas = cabecalhoOrdemRepository
            .findByStatusOrderByIdDesc(OrdemStatus.CONCLUIDA);
        
        for (CabecalhoOrdem os : osConcluidas) {
            if (os.getDataFechamento() != null && !os.getDataFechamento().isEmpty()) {
                try {
                    LocalDate dataFechamento = LocalDate.parse(os.getDataFechamento());
                    if (dataFechamento.isAfter(primeiroDiaMes.minusDays(1))) {
                        osConcluidasMes++;
                    }
                } catch (Exception e) {
                    // Ignora erros de parsing
                }
            }
        }
        
        // Buscar fornecedores
        int totalFornecedores = fornecedorRepo.findAll().size();
        
        // Filtrar apenas motores ativos
        long totalMotoresAtivos = todosMotores.stream()
            .filter(m -> m.getStatus() != null && m.getStatus())
            .count();
        
        SupervisorStatsDTO stats = new SupervisorStatsDTO();
        stats.setTotalMotores((int) totalMotoresAtivos);
        stats.setMotoresTboProximo(motoresTboProximo);
        stats.setMotoresTboExpirado(motoresTboExpirado);
        stats.setOsPendentes(osPendentes.size());
        stats.setOsEmAndamento(osAndamento.size());
        stats.setOsConcluidasMes(osConcluidasMes);
        stats.setTotalFornecedores(totalFornecedores);
        
        return ResponseEntity.ok(stats);
    }

    @Operation(
        summary = "Buscar motores em alerta (TBO próximo ou expirado)",
        description = "Retorna motores que estão próximos do TBO (>= 80%) ou com TBO expirado (> 100%), ordenados por urgência."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Motores encontrados"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas supervisores podem acessar")
    })
    @GetMapping("/supervisor/motores-alerta")
    public ResponseEntity<List<MotorAlertaDTO>> getMotoresAlerta(
            @RequestParam(defaultValue = "5") int limit) {
        List<Motor> todosMotores = motorRepository.findAll();
        List<MotorAlertaDTO> motoresAlerta = new ArrayList<>();
        
        for (Motor motor : todosMotores) {
            if (motor.getStatus() != null && motor.getStatus()) { // Apenas motores ativos
                TipoMotor tipoMotor = tipoMotorRepository.findByMarcaAndModelo(motor.getMarca(), motor.getModelo());
                if (tipoMotor != null && tipoMotor.getTbo() > 0) {
                    float percentual = (float) motor.getHoras_operacao() / tipoMotor.getTbo() * 100;
                    
                    // Incluir apenas motores com TBO >= 80%
                    if (percentual >= 80) {
                        MotorAlertaDTO dto = new MotorAlertaDTO();
                        dto.setId(motor.getId());
                        dto.setSerieMotor(motor.getSerie_motor());
                        dto.setMarca(motor.getMarca());
                        dto.setModelo(motor.getModelo());
                        dto.setHorasOperacao(motor.getHoras_operacao());
                        dto.setTbo(tipoMotor.getTbo());
                        dto.setPercentualTbo(percentual);
                        
                        if (percentual >= 100) {
                            dto.setStatusAlerta("EXPIRADO");
                        } else {
                            dto.setStatusAlerta("PROXIMO");
                        }
                        
                        if (motor.getCliente() != null) {
                            dto.setClienteNome(motor.getCliente().getName());
                        }
                        
                        motoresAlerta.add(dto);
                    }
                }
            }
        }
        
        // Ordenar: expirados primeiro, depois por percentual descendente
        motoresAlerta.sort((a, b) -> {
            if (a.getStatusAlerta().equals("EXPIRADO") && !b.getStatusAlerta().equals("EXPIRADO")) {
                return -1;
            }
            if (!a.getStatusAlerta().equals("EXPIRADO") && b.getStatusAlerta().equals("EXPIRADO")) {
                return 1;
            }
            return Float.compare(b.getPercentualTbo(), a.getPercentualTbo());
        });
        
        // Limitar quantidade
        List<MotorAlertaDTO> limitedList = motoresAlerta.stream()
            .limit(limit)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(limitedList);
    }

    @Operation(
        summary = "Buscar OS pendentes para supervisão",
        description = "Retorna as últimas ordens de serviço pendentes ordenadas por data de abertura (mais antigas primeiro)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ordens encontradas"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas supervisores podem acessar")
    })
    @GetMapping("/supervisor/os-pendentes")
    public ResponseEntity<List<CabecalhoOrdemDTO>> getOsPendentes(
            @RequestParam(defaultValue = "5") int limit) {
        List<CabecalhoOrdem> list = cabecalhoOrdemRepository
            .findByStatusOrderByIdDesc(OrdemStatus.PENDENTE);
        
        // Ordenar por data de abertura (mais antigas primeiro)
        list.sort((a, b) -> {
            if (a.getDataAbertura() == null && b.getDataAbertura() == null) return 0;
            if (a.getDataAbertura() == null) return 1;
            if (b.getDataAbertura() == null) return -1;
            try {
                LocalDate dataA = LocalDate.parse(a.getDataAbertura());
                LocalDate dataB = LocalDate.parse(b.getDataAbertura());
                return dataA.compareTo(dataB);
            } catch (Exception e) {
                return 0;
            }
        });
        
        // Limitar quantidade
        List<CabecalhoOrdem> limitedList = list.stream()
            .limit(limit)
            .collect(Collectors.toList());
        
        List<CabecalhoOrdemDTO> dtos = new ArrayList<>();
        for (CabecalhoOrdem entity : limitedList) {
            CabecalhoOrdemDTO dto = convertToDTO(entity);
            dtos.add(dto);
        }
        return ResponseEntity.ok(dtos);
    }

    @Operation(
        summary = "Buscar alertas de conformidade para auditoria",
        description = "Retorna alertas críticos de conformidade, incluindo motores com TBO expirado e OS pendentes há muito tempo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alertas encontrados"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas auditores podem acessar")
    })
    @GetMapping("/auditor/alertas-conformidade")
    public ResponseEntity<List<AlertaConformidadeDTO>> getAlertasConformidade(
            @RequestParam(defaultValue = "10") int limit) {
        List<AlertaConformidadeDTO> alertas = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate seteDiasAtras = now.minusDays(7);
        
        // Alertas de motores com TBO expirado
        List<Motor> todosMotores = motorRepository.findAll();
        for (Motor motor : todosMotores) {
            if (motor.getStatus() != null && motor.getStatus()) {
                TipoMotor tipoMotor = tipoMotorRepository.findByMarcaAndModelo(motor.getMarca(), motor.getModelo());
                if (tipoMotor != null && tipoMotor.getTbo() > 0) {
                    float percentual = (float) motor.getHoras_operacao() / tipoMotor.getTbo() * 100;
                    if (percentual >= 100) {
                        AlertaConformidadeDTO alerta = new AlertaConformidadeDTO();
                        alerta.setTipo("TBO_EXPIRADO");
                        alerta.setSeveridade("CRITICO");
                        alerta.setTitulo("TBO Excedido");
                        alerta.setDescricao(String.format("Motor %s (%s %s) excedeu o TBO (%.1f%%)", 
                            motor.getSerie_motor(), motor.getMarca(), motor.getModelo(), percentual));
                        alerta.setMotorId(motor.getId());
                        alerta.setMotorSerie(motor.getSerie_motor());
                        alerta.setData(now.toString());
                        alertas.add(alerta);
                    }
                }
            }
        }
        
        // Alertas de OS pendentes há mais de 7 dias
        List<CabecalhoOrdem> osPendentes = cabecalhoOrdemRepository
            .findByStatusOrderByIdDesc(OrdemStatus.PENDENTE);
        for (CabecalhoOrdem os : osPendentes) {
            if (os.getDataAbertura() != null && !os.getDataAbertura().isEmpty()) {
                try {
                    LocalDate dataAbertura = LocalDate.parse(os.getDataAbertura());
                    if (dataAbertura.isBefore(seteDiasAtras) || dataAbertura.isEqual(seteDiasAtras)) {
                        long diasPendente = java.time.temporal.ChronoUnit.DAYS.between(dataAbertura, now);
                        AlertaConformidadeDTO alerta = new AlertaConformidadeDTO();
                        alerta.setTipo("OS_PENDENTE_CRITICA");
                        alerta.setSeveridade(diasPendente > 14 ? "CRITICO" : "ALTO");
                        alerta.setTitulo("OS Pendente Crítica");
                        alerta.setDescricao(String.format("OS #%d está pendente há %d dias", os.getId(), diasPendente));
                        alerta.setOsId(os.getId());
                        alerta.setData(dataAbertura.toString());
                        if (os.getNumSerieMotor() != null) {
                            alerta.setMotorId(os.getNumSerieMotor().getId());
                            alerta.setMotorSerie(os.getNumSerieMotor().getSerie_motor());
                        }
                        alertas.add(alerta);
                    }
                } catch (Exception e) {
                    // Ignora erros de parsing
                }
            }
        }
        
        // Ordenar: críticos primeiro, depois por data (mais antigos primeiro)
        alertas.sort((a, b) -> {
            if (a.getSeveridade().equals("CRITICO") && !b.getSeveridade().equals("CRITICO")) {
                return -1;
            }
            if (!a.getSeveridade().equals("CRITICO") && b.getSeveridade().equals("CRITICO")) {
                return 1;
            }
            return a.getData().compareTo(b.getData());
        });
        
        // Limitar quantidade
        List<AlertaConformidadeDTO> limitedList = alertas.stream()
            .limit(limit)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(limitedList);
    }

    @Operation(
        summary = "Buscar resumo de riscos identificados para auditoria",
        description = "Retorna um resumo dos riscos identificados no sistema, incluindo motores com TBO expirado e OS pendentes críticas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Riscos encontrados"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas auditores podem acessar")
    })
    @GetMapping("/auditor/riscos")
    public ResponseEntity<RiscosDTO> getRiscos() {
        RiscosDTO riscos = new RiscosDTO();
        LocalDate now = LocalDate.now();
        LocalDate seteDiasAtras = now.minusDays(7);
        
        // Calcular motores com TBO expirado
        List<Motor> todosMotores = motorRepository.findAll();
        int motoresTboExpirado = 0;
        for (Motor motor : todosMotores) {
            if (motor.getStatus() != null && motor.getStatus()) {
                TipoMotor tipoMotor = tipoMotorRepository.findByMarcaAndModelo(motor.getMarca(), motor.getModelo());
                if (tipoMotor != null && tipoMotor.getTbo() > 0) {
                    float percentual = (float) motor.getHoras_operacao() / tipoMotor.getTbo() * 100;
                    if (percentual >= 100) {
                        motoresTboExpirado++;
                    }
                }
            }
        }
        riscos.setMotoresTboExpirado(motoresTboExpirado);
        
        // Calcular OS pendentes críticas
        List<CabecalhoOrdem> osPendentes = cabecalhoOrdemRepository
            .findByStatusOrderByIdDesc(OrdemStatus.PENDENTE);
        int osPendentesCriticas = 0;
        for (CabecalhoOrdem os : osPendentes) {
            if (os.getDataAbertura() != null && !os.getDataAbertura().isEmpty()) {
                try {
                    LocalDate dataAbertura = LocalDate.parse(os.getDataAbertura());
                    if (dataAbertura.isBefore(seteDiasAtras) || dataAbertura.isEqual(seteDiasAtras)) {
                        osPendentesCriticas++;
                    }
                } catch (Exception e) {
                    // Ignora erros de parsing
                }
            }
        }
        riscos.setOsPendentesCriticas(osPendentesCriticas);
        
        // Calcular taxa de conclusão
        List<CabecalhoOrdem> todasOs = cabecalhoOrdemRepository.findAllByOrderByIdDesc();
        List<CabecalhoOrdem> osConcluidas = cabecalhoOrdemRepository
            .findByStatusOrderByIdDesc(OrdemStatus.CONCLUIDA);
        float taxaConclusao = 0;
        if (todasOs.size() > 0) {
            taxaConclusao = (float) osConcluidas.size() / todasOs.size() * 100;
        }
        riscos.setTaxaConclusao(taxaConclusao);
        riscos.setTaxaConclusaoBaixa(taxaConclusao < 50); // Considerar baixa se < 50%
        
        // Total de riscos
        int totalRiscos = motoresTboExpirado + osPendentesCriticas;
        if (riscos.isTaxaConclusaoBaixa()) {
            totalRiscos++;
        }
        riscos.setTotalRiscos(totalRiscos);
        
        return ResponseEntity.ok(riscos);
    }

    private CabecalhoOrdemDTO convertToDTO(CabecalhoOrdem entity) {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setId(entity.getId());
        dto.setDataAbertura(entity.getDataAbertura());
        dto.setDataFechamento(entity.getDataFechamento());
        dto.setDescricao(entity.getDescricao());
        dto.setTipo(entity.getTipo());
        dto.setTempoUsado(entity.getTempoUsado());
        dto.setTempoEstimado(entity.getTempoEstimado());
        dto.setStatus(entity.getStatus().getStatus());
        dto.setValorHora(entity.getValorHora());
        if (entity.getCliente() != null) {
            dto.setClienteId(entity.getCliente().getCpf());
            dto.setClienteNome(entity.getCliente().getName());
        }
        if (entity.getNumSerieMotor() != null) {
            dto.setMotorId(String.valueOf(entity.getNumSerieMotor().getId()));
            dto.setMotorNome(entity.getNumSerieMotor().getSerie_motor());
            dto.setHorasOperacaoMotor(entity.getNumSerieMotor().getHoras_operacao());
        }
        if (entity.getSupervisor() != null) {
            dto.setSupervisorId(String.valueOf(entity.getSupervisor().getId()));
            dto.setSupervisorNome(entity.getSupervisor().getName());
        }
        if (entity.getEngenheiroAtuante() != null) {
            dto.setEngenheiroAtuanteId(String.valueOf(entity.getEngenheiroAtuante().getId()));
            dto.setEngenheiroAtuanteNome(entity.getEngenheiroAtuante().getName());
        }
        dto.setLinhas(linhaOrdemService.findByCabecalhoId(entity.getId()));
        return dto;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCabecalho(@RequestParam int id) {
        Optional<CabecalhoOrdem> opt = cabecalhoOrdemRepository.findById(id);
        if (opt.isPresent()) {
            cabecalhoOrdemRepository.deleteById(id);
            return ResponseEntity.ok("CabecalhoOrdem deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CabecalhoOrdem not found");
    }

    @PutMapping("/atualizar-status")
    public ResponseEntity<String> atualizarStatus(
            @RequestParam int cabecalhoId,
            @RequestParam int status) {
        return cabecalhoOrdemService.atualizarStatusCabecalho(cabecalhoId, status);
    }
    
    /**
     * Faz upload de um anexo para uma ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @param file Arquivo a ser anexado
     * @return URL do arquivo no Azure Blob Storage
     */
    @PostMapping("/{cabecalhoId}/anexos")
    public ResponseEntity<Map<String, String>> uploadAnexo(
            @PathVariable int cabecalhoId,
            @RequestParam("file") MultipartFile file) {
        
        System.out.println("Iniciando upload de anexo para ordem: " + cabecalhoId);
        System.out.println("Nome do arquivo: " + file.getOriginalFilename());
        System.out.println("Tamanho do arquivo: " + file.getSize() + " bytes");
        
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            System.out.println("Erro: Ordem de serviço não encontrada - ID: " + cabecalhoId);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            // Usa o ID da ordem como prefixo para organização dos arquivos
            String prefix = "ordem_" + cabecalhoId;
            System.out.println("Iniciando upload para Azure Blob Storage com prefixo: " + prefix);
            
            String fileUrl = azureBlobStorageService.uploadFile(file, prefix);
            System.out.println("Upload concluído com sucesso. URL: " + fileUrl);
            
            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            response.put("message", "Anexo adicionado com sucesso à ordem de serviço " + cabecalhoId);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.err.println("Erro durante o upload: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Falha ao fazer upload do anexo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lista todos os anexos de uma ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @return Lista de nomes de arquivos anexados à ordem
     */
    @GetMapping("/{cabecalhoId}/anexos")
    public ResponseEntity<?> listarAnexos(@PathVariable int cabecalhoId) {
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        String prefix = "ordem_" + cabecalhoId;
        List<String> arquivos = azureBlobStorageService.listFilesWithPrefix(prefix);
        
        List<Map<String, String>> resultados = new ArrayList<>();
        for (String nomeArquivo : arquivos) {
            Map<String, String> arquivoInfo = new HashMap<>();
            arquivoInfo.put("nome", nomeArquivo);
            arquivoInfo.put("url", azureBlobStorageService.getFileUrl(nomeArquivo));
            resultados.add(arquivoInfo);
        }
        
        return ResponseEntity.ok(resultados);
    }
    
    /**
     * Faz o download de um anexo específico de uma ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @param nomeArquivo Nome do arquivo a ser baixado
     * @return Arquivo para download
     */
    @GetMapping("/{cabecalhoId}/anexos/{nomeArquivo}")
    public ResponseEntity<?> downloadAnexo(
            @PathVariable int cabecalhoId,
            @PathVariable String nomeArquivo) {
        
        System.out.println("Iniciando download de anexo: " + nomeArquivo + " da ordem: " + cabecalhoId);
        
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Verifica se o arquivo existe
        if (!azureBlobStorageService.fileExists(nomeArquivo)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Arquivo não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            // Obtém o cliente de blob para o arquivo
            BlobClient blobClient = azureBlobStorageService.getBlobClient(nomeArquivo);
            
            // Obtém as propriedades do blob para determinar o tipo de conteúdo
            String contentType = blobClient.getProperties().getContentType();
            if (contentType == null || contentType.isEmpty()) {
                // Se não tiver tipo de conteúdo definido, tenta inferir pelo nome do arquivo
                contentType = inferContentType(nomeArquivo);
            }
            
            // Cria um array de bytes com o conteúdo do arquivo
            byte[] content = blobClient.downloadContent().toBytes();
            
            // Configura a resposta HTTP
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + extractOriginalFilename(nomeArquivo) + "\"")
                    .body(content);
            
        } catch (Exception e) {
            System.err.println("Erro ao fazer download do arquivo: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao processar o download: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Extrai o nome original do arquivo do nome completo armazenado no Azure
     * 
     * @param fullFileName Nome completo do arquivo (com prefixo e UUID)
     * @return Nome original do arquivo
     */
    private String extractOriginalFilename(String fullFileName) {
        // Os nomes dos arquivos estão no formato: prefixo_UUID_nomeOriginal
        // Vamos pegar tudo após o segundo underscore
        int secondUnderscoreIndex = fullFileName.indexOf('_', fullFileName.indexOf('_') + 1);
        if (secondUnderscoreIndex > 0 && secondUnderscoreIndex < fullFileName.length() - 1) {
            return fullFileName.substring(secondUnderscoreIndex + 1);
        }
        return fullFileName; // Fallback para o nome completo
    }
    
    /**
     * Infere o tipo de conteúdo com base na extensão do arquivo
     * 
     * @param fileName Nome do arquivo
     * @return Tipo de conteúdo MIME
     */
    private String inferContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }
    
    /**
     * Exclui um anexo específico de uma ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @param nomeArquivo Nome do arquivo a ser excluído
     * @return Mensagem de confirmação
     */
    @DeleteMapping("/{cabecalhoId}/anexos/{nomeArquivo}")
    public ResponseEntity<?> excluirAnexo(
            @PathVariable int cabecalhoId,
            @PathVariable String nomeArquivo) {
        
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Verifica se o arquivo existe
        if (!azureBlobStorageService.fileExists(nomeArquivo)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Arquivo não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Exclui o arquivo
        azureBlobStorageService.deleteFile(nomeArquivo);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Arquivo excluído com sucesso");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gera um PDF com todos os detalhes da ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @return Arquivo PDF para download
     */
    @GetMapping("/{cabecalhoId}/pdf")
    public ResponseEntity<?> gerarPdfOrdemServico(@PathVariable int cabecalhoId) {
        System.out.println("Iniciando geração de PDF para ordem: " + cabecalhoId);
        
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            // Gerar o PDF usando o serviço
            byte[] pdfBytes = ordemServicoPdfService.gerarPdfOrdemServico(cabecalhoId);
            
            // Nome do arquivo para download
            String fileName = "ordem_servico_" + cabecalhoId + ".pdf";
            
            // Retornar o PDF como download
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + fileName + "\"")
                    .body(pdfBytes);
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar PDF: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao gerar PDF da ordem de serviço: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
