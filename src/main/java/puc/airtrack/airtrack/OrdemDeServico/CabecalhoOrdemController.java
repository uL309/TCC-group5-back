package puc.airtrack.airtrack.OrdemDeServico;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import puc.airtrack.airtrack.Cliente.ClienteRepo;
import puc.airtrack.airtrack.Motor.MotorRepository;
import puc.airtrack.airtrack.Login.UserService;

@RestController
@RequestMapping("/ordem")
public class CabecalhoOrdemController {
    @Autowired
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;
    @Autowired
    private ClienteRepo clienteRepo;
    @Autowired
    private MotorRepository motorRepository;
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<String> createCabecalho(@RequestBody CabecalhoOrdemDTO dto) {
        if (dto != null) {
            CabecalhoOrdem entity = new CabecalhoOrdem();
            entity.setDataAbertura(dto.getDataAbertura());
            entity.setDataFechamento(dto.getDataFechamento());
            entity.setDescricao(dto.getDescricao());
            entity.setTempoUsado(dto.getTempoUsado());
            entity.setStatus(dto.getStatus());
            if (dto.getClienteId() != null) {
                entity.setCliente(clienteRepo.findById(dto.getClienteId()).orElse(null));
            }
            if (dto.getMotorId() != null) {
                try {
                    int motorId = Integer.parseInt(dto.getMotorId());
                    entity.setNumSerieMotor(motorRepository.findById(motorId).orElse(null));
                } catch (NumberFormatException e) {
                    entity.setNumSerieMotor(null);
                }
            }
            if (dto.getSupervisorId() != null) {
                try {
                    int supervisorId = Integer.parseInt(dto.getSupervisorId());
                    entity.setSupervisor(userService.findById(supervisorId));
                } catch (NumberFormatException e) {
                    entity.setSupervisor(null);
                }
            }
            cabecalhoOrdemRepository.save(entity);
            URI location = URI.create("/ordem/get?id=" + entity.getId());
            return ResponseEntity.created(location).body("CabecalhoOrdem created successfully");
        }
        return ResponseEntity.badRequest().body("Invalid data");
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateCabecalho(@RequestBody CabecalhoOrdemDTO dto) {
        if (dto != null && dto.getId() != null) {
            Optional<CabecalhoOrdem> opt = cabecalhoOrdemRepository.findById(dto.getId());
            if (opt.isPresent()) {
                CabecalhoOrdem entity = opt.get();
                entity.setDataAbertura(dto.getDataAbertura());
                entity.setDataFechamento(dto.getDataFechamento());
                entity.setDescricao(dto.getDescricao());
                entity.setTempoUsado(dto.getTempoUsado());
                entity.setStatus(dto.getStatus());
                if (dto.getClienteId() != null) {
                    entity.setCliente(clienteRepo.findById(dto.getClienteId()).orElse(null));
                }
                if (dto.getMotorId() != null) {
                    try {
                        int motorId = Integer.parseInt(dto.getMotorId());
                        entity.setNumSerieMotor(motorRepository.findById(motorId).orElse(null));
                    } catch (NumberFormatException e) {
                        entity.setNumSerieMotor(null);
                    }
                }
                if (dto.getSupervisorId() != null) {
                    try {
                        int supervisorId = Integer.parseInt(dto.getSupervisorId());
                        entity.setSupervisor(userService.findById(supervisorId));
                    } catch (NumberFormatException e) {
                        entity.setSupervisor(null);
                    }
                }
                cabecalhoOrdemRepository.save(entity);
                return ResponseEntity.ok("CabecalhoOrdem updated successfully");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CabecalhoOrdem not found");
    }

    @GetMapping("/get")
    public ResponseEntity<CabecalhoOrdemDTO> getCabecalho(@RequestParam int id) {
        Optional<CabecalhoOrdem> opt = cabecalhoOrdemRepository.findById(id);
        if (opt.isPresent()) {
            CabecalhoOrdem entity = opt.get();
            CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
            dto.setId(entity.getId());
            dto.setDataAbertura(entity.getDataAbertura());
            dto.setDataFechamento(entity.getDataFechamento());
            dto.setDescricao(entity.getDescricao());
            dto.setTempoUsado(entity.getTempoUsado());
            dto.setStatus(entity.getStatus());
            if (entity.getCliente() != null) {
                dto.setClienteId(entity.getCliente().getId());
            }
            if (entity.getNumSerieMotor() != null) {
                dto.setMotorId(String.valueOf(entity.getNumSerieMotor().getId()));
            }
            if (entity.getSupervisor() != null) {
                dto.setSupervisorId(String.valueOf(entity.getSupervisor().getId()));
            }
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/list")
    public ResponseEntity<List<CabecalhoOrdemDTO>> getAllCabecalhos() {
        List<CabecalhoOrdem> list = cabecalhoOrdemRepository.findAll();
        List<CabecalhoOrdemDTO> dtos = new ArrayList<>();
        for (CabecalhoOrdem entity : list) {
            CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
            dto.setId(entity.getId());
            dto.setDataAbertura(entity.getDataAbertura());
            dto.setDataFechamento(entity.getDataFechamento());
            dto.setDescricao(entity.getDescricao());
            dto.setTempoUsado(entity.getTempoUsado());
            dto.setStatus(entity.getStatus());
            if (entity.getCliente() != null) {
                dto.setClienteId(entity.getCliente().getId());
            }
            if (entity.getNumSerieMotor() != null) {
                dto.setMotorId(String.valueOf(entity.getNumSerieMotor().getId()));
            }
            if (entity.getSupervisor() != null) {
                dto.setSupervisorId(String.valueOf(entity.getSupervisor().getId()));
            }
            dtos.add(dto);
        }
        return ResponseEntity.ok(dtos);
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
}
