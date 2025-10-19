package puc.airtrack.airtrack.OrdemDeServico;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import puc.airtrack.airtrack.Pecas.PecasRepository;
import puc.airtrack.airtrack.Login.UserService;

@RestController
@RequestMapping("/linhaordem")
@Tag(name = "Linha de Ordem", description = "Gerenciamento de itens/peças utilizadas em ordens de serviço")
@SecurityRequirement(name = "bearerAuth")
public class LinhaOrdemController {
    @Autowired
    private LinhaOrdemRepository linhaOrdemRepository;
    @Autowired
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;
    @Autowired
    private PecasRepository pecasRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private LinhaOrdemService linhaOrdemService;

    @Operation(
        summary = "Adicionar item à ordem",
        description = "Adiciona uma peça/serviço a uma ordem de serviço específica, com quantidade e tempo gasto.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do item da ordem",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LinhaOrdemDTO.class),
                examples = @ExampleObject(
                    name = "Filtro substituído",
                    value = """
                    {
                      "ordemId": 1,
                      "pecaId": 1,
                      "quantidade": 1,
                      "tempoGasto": 2.5,
                      "engenheiroId": "3"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Item adicionado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @PostMapping("/create")
    public ResponseEntity<String> createLinhaOrdem(@RequestBody LinhaOrdemDTO dto) {
        try {
            LinhaOrdemDTO savedDto = linhaOrdemService.create(dto);
            URI location = URI.create("/linhaordem/get?id=" + savedDto.getId());
            return ResponseEntity.created(location).body("LinhaOrdem created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
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
        return linhaOrdemService.findDtoById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/list")
    public ResponseEntity<List<LinhaOrdemDTO>> getAllLinhaOrdem() {
        return ResponseEntity.ok(linhaOrdemService.findAllDto());
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
        return ResponseEntity.ok(linhaOrdemService.findByCabecalhoId(cabecalhoId));
    }
}
