package puc.airtrack.airtrack.OrdemDeServico;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import puc.airtrack.airtrack.Cliente.ClienteRepo;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.Motor.MotorRepository;

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
    @Autowired
    private LinhaOrdemService linhaOrdemService;
    @Autowired
    private CabecalhoOrdemService cabecalhoOrdemService;

    @PostMapping("/create")
    public ResponseEntity<String> createCabecalho(@RequestBody CabecalhoOrdemDTO dto) {
        return cabecalhoOrdemService.createCabecalho(dto);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateCabecalho(@RequestBody CabecalhoOrdemDTO dto) {
        return cabecalhoOrdemService.updateCabecalho(dto);
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
                dto.setClienteId(entity.getCliente().getCpf());
                dto.setClienteNome(entity.getCliente().getName());
            }
            if (entity.getNumSerieMotor() != null) {
                dto.setMotorId(String.valueOf(entity.getNumSerieMotor().getId()));
                dto.setMotorNome(entity.getNumSerieMotor().getSerie_motor());
            }
            if (entity.getSupervisor() != null) {
                dto.setSupervisorId(String.valueOf(entity.getSupervisor().getId()));
                dto.setSupervisorNome(entity.getSupervisor().getName());
            }
            dto.setLinhas(linhaOrdemService.findByCabecalhoId(id));
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
                dto.setClienteId(entity.getCliente().getCpf());
                dto.setClienteNome(entity.getCliente().getName());
            }
            if (entity.getNumSerieMotor() != null) {
                dto.setMotorId(String.valueOf(entity.getNumSerieMotor().getId()));
                dto.setMotorNome(entity.getNumSerieMotor().getSerie_motor());
            }
            if (entity.getSupervisor() != null) {
                dto.setSupervisorId(String.valueOf(entity.getSupervisor().getId()));
                dto.setSupervisorNome(entity.getSupervisor().getName());
            }
            System.out.println("Adding CabecalhoOrdemDTO: " + dto);
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

    @PutMapping("/atualizar-status")
    public ResponseEntity<String> atualizarStatus(
            @RequestParam int cabecalhoId,
            @RequestParam int status) {
        return cabecalhoOrdemService.atualizarStatusCabecalho(cabecalhoId, status);
    }
}
