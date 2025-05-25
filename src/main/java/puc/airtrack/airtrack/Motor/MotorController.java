package puc.airtrack.airtrack.Motor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/motores")
public class MotorController {

    @Autowired
    private MotorRepository motorRepository;

    @GetMapping
    public List<Motor> listarTodos() {
        return motorRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Motor> buscarPorId(@PathVariable int id) {
        return motorRepository.findById(id);
    }

    @PostMapping
    public Motor criar(@RequestBody Motor motor) {
        return motorRepository.save(motor);
    }

    @PutMapping("/{id}")
    public Motor atualizar(@PathVariable int id, @RequestBody Motor motor) {
        motor.setId(id);
        return motorRepository.save(motor);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable int id) {
        motorRepository.deleteById(id);
    }
}
