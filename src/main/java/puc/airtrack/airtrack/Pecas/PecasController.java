package puc.airtrack.airtrack.Pecas;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import puc.airtrack.airtrack.Fornecedor.*;


@RestController
public class PecasController {

    @Autowired
    private PecasRepository pecasRepository;

    @Autowired
    private FornecedorRepo fornecedorRepository;

    @PostMapping("/cpeca")
    public ResponseEntity<String> createPeca(@RequestBody PecasDTO entity) {
        if (entity != null) {
            Pecas pecas = new Pecas();
            URI location;
            pecas.setNome(entity.getNome());
            pecas.setNum_serie(entity.getNum_serie());
            pecas.setData_aquisicao(entity.getData_aquisicao());
            pecas.setStatus(entity.getStatus());
            pecas.setCategoria(entity.getCategoria());
            pecas.setId_engenheiro(entity.getId_engenheiro());
            if (entity.getFornecedorId() != null) {
                Optional<Fornecedor> fornecedor = fornecedorRepository.findById(entity.getFornecedorId().toString());
                fornecedor.ifPresent(pecas::setFornecedor);
            }
            pecasRepository.save(pecas);
            location = URI.create("/gpeca?param=" + pecas.getId());
            return ResponseEntity.created(location).body("Peca created successfully");
        }
        return ResponseEntity.badRequest().body("Peca already exists");
    }

    @PostMapping("/upeca")
    public ResponseEntity<String> updatePeca(@RequestBody PecasDTO entity) {
        if (entity != null) {
            Optional<Pecas> optionalPecas = pecasRepository.findById(entity.getId());
            if (optionalPecas.isPresent()) {
                Pecas pecas = optionalPecas.get();
                pecas.setNome(entity.getNome());
                pecas.setNum_serie(entity.getNum_serie());
                pecas.setData_aquisicao(entity.getData_aquisicao());
                pecas.setStatus(entity.getStatus());
                pecas.setCategoria(entity.getCategoria());
                pecas.setId_engenheiro(entity.getId_engenheiro());
                if (entity.getFornecedorId() != null) {
                    Optional<Fornecedor> fornecedor = fornecedorRepository.findById(entity.getFornecedorId().toString());
                    fornecedor.ifPresent(pecas::setFornecedor);
                } else {
                    pecas.setFornecedor(null);
                }
                pecasRepository.save(pecas);
                return ResponseEntity.ok("Peca updated successfully");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Peca not found");
    }

    @GetMapping("/gpeca")
    public ResponseEntity<PecasDTO> getPeca(@RequestParam int id) {
        Optional<Pecas> optionalPecas = pecasRepository.findById(id);
        if (optionalPecas.isPresent()) {
            Pecas pecas = optionalPecas.get();
            PecasDTO dto = new PecasDTO();
            dto.setId(pecas.getId());
            dto.setNome(pecas.getNome());
            dto.setNum_serie(pecas.getNum_serie());
            dto.setData_aquisicao(pecas.getData_aquisicao());
            dto.setStatus(pecas.getStatus());
            dto.setCategoria(pecas.getCategoria());
            dto.setId_engenheiro(pecas.getId_engenheiro());
            dto.setFornecedorId(pecas.getFornecedor() != null ? Integer.valueOf(pecas.getFornecedor().getId()) : null);
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/gpecas")
    public ResponseEntity<List<PecasDTO>> getAllPecas() {
        List<Pecas> pecasList = pecasRepository.findAll();
        List<PecasDTO> pecasDTOs = new ArrayList<>();
        for (Pecas pecas : pecasList) {
            PecasDTO dto = new PecasDTO();
            dto.setId(pecas.getId());
            dto.setNome(pecas.getNome());
            dto.setNum_serie(pecas.getNum_serie());
            dto.setData_aquisicao(pecas.getData_aquisicao());
            dto.setStatus(pecas.getStatus());
            dto.setCategoria(pecas.getCategoria());
            dto.setId_engenheiro(pecas.getId_engenheiro());
            dto.setFornecedorId(pecas.getFornecedor() != null ? Integer.valueOf(pecas.getFornecedor().getId()) : null);
            pecasDTOs.add(dto);
        }
        return ResponseEntity.ok(pecasDTOs);
    }

    @GetMapping("/dpeca")
    public ResponseEntity<String> deletePeca(@RequestParam int id) {
        Optional<Pecas> optionalPecas = pecasRepository.findById(id);
        if (optionalPecas.isPresent()) {
            Pecas pecas = optionalPecas.get();
            pecas.setStatus("inativo");
            pecasRepository.save(pecas);
            return ResponseEntity.ok("Peca deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Peca not found");
    }
}