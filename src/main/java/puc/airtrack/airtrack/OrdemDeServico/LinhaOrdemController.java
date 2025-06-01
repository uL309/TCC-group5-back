package puc.airtrack.airtrack.OrdemDeServico;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import puc.airtrack.airtrack.Pecas.PecasRepository;
import puc.airtrack.airtrack.Login.UserService;

@RestController
@RequestMapping("/linhaordem")
public class LinhaOrdemController {
    @Autowired
    private LinhaOrdemRepository linhaOrdemRepository;
    @Autowired
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;
    @Autowired
    private PecasRepository pecasRepository;
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<String> createLinhaOrdem(@RequestBody LinhaOrdemDTO dto) {
        if (dto != null) {
            LinhaOrdem entity = new LinhaOrdem();
            entity.setQuantidade(dto.getQuantidade());
            entity.setTempoGasto(dto.getTempoGasto());
            if (dto.getOrdemId() != null) {
                entity.setOrdem(cabecalhoOrdemRepository.findById(dto.getOrdemId()).orElse(null));
            }
            if (dto.getPecaId() != null) {
                entity.setPeca(pecasRepository.findById(dto.getPecaId()).orElse(null));
            }
            if (dto.getEngenheiroId() != null) {
                try {
                    int engenheiroId = Integer.parseInt(dto.getEngenheiroId());
                    entity.setEngenheiro(userService.findById(engenheiroId));
                } catch (NumberFormatException e) {
                    entity.setEngenheiro(null);
                }
            }
            linhaOrdemRepository.save(entity);
            URI location = URI.create("/linhaordem/get?id=" + entity.getId());
            return ResponseEntity.created(location).body("LinhaOrdem created successfully");
        }
        return ResponseEntity.badRequest().body("Invalid data");
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateLinhaOrdem(@RequestBody LinhaOrdemDTO dto) {
        if (dto != null) {
            Optional<LinhaOrdem> opt = linhaOrdemRepository.findById(dto.getId());
            if (opt.isPresent()) {
                LinhaOrdem entity = opt.get();
                entity.setQuantidade(dto.getQuantidade());
                entity.setTempoGasto(dto.getTempoGasto());
                if (dto.getOrdemId() != null) {
                    entity.setOrdem(cabecalhoOrdemRepository.findById(dto.getOrdemId()).orElse(null));
                }
                if (dto.getPecaId() != null) {
                    entity.setPeca(pecasRepository.findById(dto.getPecaId()).orElse(null));
                }
                if (dto.getEngenheiroId() != null) {
                    try {
                        int engenheiroId = Integer.parseInt(dto.getEngenheiroId());
                        entity.setEngenheiro(userService.findById(engenheiroId));
                    } catch (NumberFormatException e) {
                        entity.setEngenheiro(null);
                    }
                }
                linhaOrdemRepository.save(entity);
                return ResponseEntity.ok("LinhaOrdem updated successfully");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("LinhaOrdem not found");
    }

    @GetMapping("/get")
    public ResponseEntity<LinhaOrdemDTO> getLinhaOrdem(@RequestParam int id) {
        Optional<LinhaOrdem> opt = linhaOrdemRepository.findById(id);
        if (opt.isPresent()) {
            LinhaOrdem entity = opt.get();
            LinhaOrdemDTO dto = new LinhaOrdemDTO();
            dto.setId(entity.getId());
            dto.setQuantidade(entity.getQuantidade());
            dto.setTempoGasto(entity.getTempoGasto());
            if (entity.getOrdem() != null) {
                dto.setOrdemId(entity.getOrdem().getId());
            }
            if (entity.getPeca() != null) {
                dto.setPecaId(entity.getPeca().getId());
            }
            if (entity.getEngenheiro() != null) {
                dto.setEngenheiroId(String.valueOf(entity.getEngenheiro().getId()));
            }
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/list")
    public ResponseEntity<List<LinhaOrdemDTO>> getAllLinhaOrdem() {
        List<LinhaOrdem> list = linhaOrdemRepository.findAll();
        List<LinhaOrdemDTO> dtos = new ArrayList<>();
        for (LinhaOrdem entity : list) {
            LinhaOrdemDTO dto = new LinhaOrdemDTO();
            dto.setId(entity.getId());
            dto.setQuantidade(entity.getQuantidade());
            dto.setTempoGasto(entity.getTempoGasto());
            if (entity.getOrdem() != null) {
                dto.setOrdemId(entity.getOrdem().getId());
            }
            if (entity.getPeca() != null) {
                dto.setPecaId(entity.getPeca().getId());
            }
            if (entity.getEngenheiro() != null) {
                dto.setEngenheiroId(String.valueOf(entity.getEngenheiro().getId()));
            }
            dtos.add(dto);
        }
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteLinhaOrdem(@RequestParam int id) {
        Optional<LinhaOrdem> opt = linhaOrdemRepository.findById(id);
        if (opt.isPresent()) {
            linhaOrdemRepository.deleteById(id);
            return ResponseEntity.ok("LinhaOrdem deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("LinhaOrdem not found");
    }

    @GetMapping("/bycabecalho")
    public ResponseEntity<List<LinhaOrdemDTO>> getByCabecalho(@RequestParam Integer cabecalhoId) {
        List<LinhaOrdem> list = linhaOrdemRepository.findByOrdem_Id(cabecalhoId);
        List<LinhaOrdemDTO> dtos = new ArrayList<>();
        for (LinhaOrdem entity : list) {
            LinhaOrdemDTO dto = new LinhaOrdemDTO();
            dto.setId(entity.getId());
            dto.setQuantidade(entity.getQuantidade());
            dto.setTempoGasto(entity.getTempoGasto());
            dto.setOrdemId(entity.getOrdem() != null ? entity.getOrdem().getId() : null);
            if (entity.getPeca() != null) {
                dto.setPecaId(entity.getPeca().getId());
            }
            if (entity.getEngenheiro() != null) {
                dto.setEngenheiroId(String.valueOf(entity.getEngenheiro().getId()));
            }
            dtos.add(dto);
        }
        return ResponseEntity.ok(dtos);
    }
}
