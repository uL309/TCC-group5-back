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

import puc.airtrack.airtrack.Cliente.Cliente;
import puc.airtrack.airtrack.Cliente.ClienteRepo;
import puc.airtrack.airtrack.tipoMotor.TipoMotor;
import puc.airtrack.airtrack.tipoMotor.TipoMotorRepository;

@RestController
public class MotorController {

    @Autowired
    private MotorRepository motorRepository;
    @Autowired
    private TipoMotorRepository tipoMotorRepository;

    @Autowired
    private ClienteRepo clienteRepository;
     @PostMapping("/cmotor")
    public ResponseEntity<String> createMotor(@RequestBody MotorDTO dto) {
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

    @PutMapping("/umotor")
public ResponseEntity<String> updateMotor(@RequestBody MotorDTO dto) {
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

    @GetMapping("/gmotor")
    public ResponseEntity<Motor> buscarPorId(@RequestParam("param") Integer id) {
        Optional<Motor> motor = motorRepository.findById(id);
        return motor.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(404).body(null));
    }


    @DeleteMapping("/dmotor")
    public ResponseEntity<String> deletar(@RequestParam("param") Integer id) {
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