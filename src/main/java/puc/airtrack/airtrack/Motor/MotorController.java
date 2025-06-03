package puc.airtrack.airtrack.Motor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
public class MotorController {

    @Autowired
    private MotorRepository motorRepository;

    @PostMapping("/cmotor")
    public ResponseEntity<String> cadastrar(@RequestBody Motor motor) {
        if (motor == null || motor.getSerie_motor() == null || motor.getSerie_motor().isEmpty()) {
            return ResponseEntity.badRequest().body("Dados inválidos para cadastro do motor.");
        }
        motorRepository.save(motor);
        URI location = URI.create("/gmotor?param=" + motor.getId());
        return ResponseEntity.created(location).body("Motor cadastrado com sucesso!");
    }

    @GetMapping("/gmotores")
    public ResponseEntity<List<Motor>> listar() {
        List<Motor> motores = motorRepository.findAll();
        return ResponseEntity.ok(motores);
    }

    @PutMapping("/umotor")
    public ResponseEntity<String> atualizar(@RequestBody Motor motor) {
        Optional<Motor> existingMotor = motorRepository.findById(motor.getId());
        if (existingMotor.isPresent()) {
            motorRepository.save(motor);
            return ResponseEntity.ok("Motor atualizado com sucesso!");
        } else {
            return ResponseEntity.status(404).body("Motor não encontrado!");
        }
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