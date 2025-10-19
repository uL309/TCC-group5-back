package puc.airtrack.airtrack.Motor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import puc.airtrack.airtrack.Cliente.Cliente;
import puc.airtrack.airtrack.Cliente.ClienteRepo;
import puc.airtrack.airtrack.tipoMotor.TipoMotor;
import puc.airtrack.airtrack.tipoMotor.TipoMotorRepository;

@RestController
@Tag(name = "Motor", description = "Gerenciamento de motores de aeronaves - Cadastro, atualização, consulta e exclusão lógica de motores")
public class MotorController {

    @Autowired
    private MotorRepository motorRepository;
    @Autowired
    private TipoMotorRepository tipoMotorRepository;

    @Autowired
    private ClienteRepo clienteRepository;
    
    @Operation(
        summary = "Criar novo motor",
        description = "Registra um novo motor no sistema com informações completas incluindo série, marca, modelo e vinculação com cliente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Motor criado com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Motor cadastrado com sucesso!\""
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos ou série do motor já existe",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Erro: Motor já cadastrado ou dados inválidos!\""
                )
            )
        )
    })
    @PostMapping("/cmotor")
    public ResponseEntity<String> createMotor(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do motor a ser criado",
            required = true,
            content = @Content(
                schema = @Schema(implementation = MotorDTO.class),
                examples = @ExampleObject(
                    name = "Exemplo Motor Pratt & Whitney",
                    value = """
                        {
                          "marca": "Pratt & Whitney",
                          "modelo": "PT6A-60A",
                          "serie_motor": "PCE-123456",
                          "data_cadastro": "2025-01-15",
                          "status": true,
                          "horas_operacao": 250,
                          "tbo": 3600,
                          "cliente_cpf": "123.456.789-00",
                          "cliente_nome": "Aviação Executiva Ltda"
                        }
                        """
                )
            )
        )
        @RequestBody MotorDTO dto) {
        if (dto != null) {
            Motor motor = new Motor();
            URI location;

            motor.setMarca(dto.getMarca());
            motor.setModelo(dto.getModelo());
            motor.setSerie_motor(dto.getSerie_motor());
            motor.setData_cadastro(dto.getData_cadastro());
            motor.setStatus(dto.getStatus());
            motor.setHoras_operacao(dto.getHoras_operacao());

            // Busca o cliente pelo ID e seta no motor
            if (dto.getCliente_cpf() != null && !dto.getCliente_cpf().isEmpty()) {
                Optional<Cliente> cliente = clienteRepository.findByCpf(dto.getCliente_cpf());
                cliente.ifPresent(motor::setCliente);
            }

            motorRepository.save(motor);
            location = URI.create("/gmotor?param=" + motor.getId());
            return ResponseEntity.created(location).body("Motor cadastrado com sucesso!");
        }
        return ResponseEntity.badRequest().body("Dados inválidos para cadastro do motor.");
    }
    @GetMapping("/gmotores")
    public ResponseEntity<List<MotorDTO>> listar() {
        List<Motor> motoresEntity = motorRepository.findAll();
        List<MotorDTO> motoresDTO = new ArrayList<>();
        for (Motor motor : motoresEntity) {
            TipoMotor tipoMotor = tipoMotorRepository.findByMarcaAndModelo(motor.getMarca(), motor.getModelo());
            MotorDTO motorDTO = new MotorDTO();
            motorDTO.setId(motor.getId());
            motorDTO.setMarca(motor.getMarca());
            motorDTO.setModelo(motor.getModelo());
            motorDTO.setSerie_motor(motor.getSerie_motor());
            motorDTO.setStatus(motor.getStatus());
            motorDTO.setHoras_operacao(motor.getHoras_operacao());
            motorDTO.setData_cadastro(motor.getData_cadastro());
            // Corrija aqui:
            if (tipoMotor != null) {
                motorDTO.setTbo(tipoMotor.getTbo());
            } else {
                motorDTO.setTbo(0); // ou outro valor padrão
            }
            if (motor.getCliente() != null) {
                motorDTO.setCliente_nome(motor.getCliente().getName());
                motorDTO.setCliente_cpf(motor.getCliente().getCpf());
            }
            motoresDTO.add(motorDTO);
        }

        return ResponseEntity.ok(motoresDTO);
    }

    @Operation(
        summary = "Atualizar motor existente",
        description = "Atualiza as informações de um motor já cadastrado no sistema, incluindo horas de operação, status e vinculação com cliente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Motor atualizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Motor atualizado com sucesso!\""
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos ou motor não encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Dados inválidos ou motor não encontrado!\""
                )
            )
        )
    })
    @PutMapping("/umotor")
    public ResponseEntity<String> updateMotor(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados do motor (incluir ID)",
            required = true,
            content = @Content(
                schema = @Schema(implementation = MotorDTO.class),
                examples = @ExampleObject(
                    name = "Atualização de Horas de Operação",
                    value = """
                        {
                          "id": 1,
                          "marca": "Pratt & Whitney",
                          "modelo": "PT6A-60A",
                          "serie_motor": "PCE-123456",
                          "data_cadastro": "2025-01-15",
                          "status": true,
                          "horas_operacao": 850,
                          "tbo": 3600,
                          "cliente_cpf": "123.456.789-00",
                          "cliente_nome": "Aviação Executiva Ltda"
                        }
                        """
                )
            )
        )
        @RequestBody MotorDTO dto) {
    if (dto != null) {
        Optional<Motor> optionalMotor = motorRepository.findById(dto.getId());
        if (optionalMotor.isPresent()) {
            Motor motor = optionalMotor.get();
            motor.setMarca(dto.getMarca());
            motor.setModelo(dto.getModelo());
            motor.setSerie_motor(dto.getSerie_motor());
            motor.setData_cadastro(dto.getData_cadastro());
            motor.setStatus(dto.getStatus());
            motor.setHoras_operacao(dto.getHoras_operacao());

            // Atualiza o cliente se informado
            if (dto.getCliente_cpf() != null && !dto.getCliente_cpf().isEmpty()) {
                Optional<Cliente> cliente = clienteRepository.findByCpf(dto.getCliente_cpf());
                cliente.ifPresent(motor::setCliente);
            } else {
                motor.setCliente(null);
            }

            motorRepository.save(motor);
            return ResponseEntity.ok("Motor atualizado com sucesso!");
        }
    }
    return ResponseEntity.badRequest().body("Dados inválidos ou motor não encontrado!");
}

    @Operation(
        summary = "Buscar motor por ID",
        description = "Retorna os dados completos de um motor específico através do seu ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Motor encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Motor.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Motor não encontrado"
        )
    })
    @GetMapping("/gmotor")
    public ResponseEntity<Motor> buscarPorId(
        @Parameter(description = "ID do motor", example = "1", required = true)
        @RequestParam("param") Integer id) {
        Optional<Motor> motor = motorRepository.findById(id);
        return motor.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(404).body(null));
    }

    @Operation(
        summary = "Deletar motor (exclusão lógica)",
        description = "Realiza a exclusão lógica do motor, marcando seu status como inativo sem remover do banco de dados"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Motor deletado com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Motor deletado com sucesso!\""
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Motor não encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Motor não encontrado!\""
                )
            )
        )
    })
    @DeleteMapping("/dmotor")
    public ResponseEntity<String> deletar(
        @Parameter(description = "ID do motor a ser deletado", example = "1", required = true)
        @RequestParam("param") Integer id) {
        Optional<Motor> existingMotor = motorRepository.findById(id);
        if (existingMotor.isPresent()) {
            existingMotor.get().setStatus(false);
            motorRepository.save(existingMotor.get());
            return ResponseEntity.ok("Motor deletado com sucesso!");
        } else {
            return ResponseEntity.status(404).body("Motor não encontrado!");
        }
    }
}