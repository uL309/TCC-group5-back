package puc.airtrack.airtrack.Motor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
public class MotorController {

    @Autowired
    private MotorRepository motorRepository;

    @PostMapping("/cmotor")
    public String cadastrar(@RequestBody Motor motor) {
        motorRepository.save(motor);
        return "Motor cadastrado com sucesso!";
    }

    @GetMapping("/gmotores")
    public List<Motor> listar() {
        return motorRepository.findAll();
    }

    @PutMapping("/umotor")
    public String atualizar(@RequestBody Motor motor) {
        motorRepository.save(motor);
        return "Motor atualizado com sucesso!";
    }

    @GetMapping("/gmotor")
    public Optional<Motor> buscarPorId(@RequestParam("param") Integer id) {
        return motorRepository.findById(id);
    }
}
