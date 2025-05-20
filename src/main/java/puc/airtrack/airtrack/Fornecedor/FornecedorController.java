package puc.airtrack.airtrack.Fornecedor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FornecedorController {
    
    @Autowired
    private FornecedorRepo fornecedorRepo;

    @PostMapping("/cforn")
    public ResponseEntity<String> createFornecedor(@RequestBody FornecedorDTO entity) {
        if (entity != null) {
            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setId(entity.getId());
            fornecedor.setName(entity.getName());
            fornecedor.setEmail(entity.getEmail());
            fornecedor.setContato(entity.getContato());
            fornecedor.setCategoria(entity.getCategoria());
            fornecedor.setStatus(entity.getStatus());
            fornecedorRepo.save(fornecedor);
            URI location = URI.create("/gforn?param=" + fornecedor.getId());
            return ResponseEntity.created(location).body("Fornecedor created successfully");
        }
        return ResponseEntity.badRequest().body("Fornecedor already exists");
    }

    @PutMapping("/uforn")
    public ResponseEntity<String> updateFornecedor(@RequestBody FornecedorDTO entity) {
        if (entity != null) {
            Optional<Fornecedor> fornecedorOpt = fornecedorRepo.findById(entity.getId());
            if (fornecedorOpt.isPresent()) {
            Fornecedor fornecedor = fornecedorOpt.get();
                if (fornecedor != null) {
                if (entity.getId() != fornecedor.getId()) {
                    fornecedor.setId(entity.getId());
                }
                if (entity.getName() != fornecedor.getName()) {
                    fornecedor.setName(entity.getName());  
                }
                if (entity.getEmail() != fornecedor.getEmail()) {
                    fornecedor.setEmail(entity.getEmail());
                }
                if (entity.getContato() != fornecedor.getContato()) {
                    fornecedor.setContato(entity.getContato());
                }
                if (entity.getCategoria() != fornecedor.getCategoria()) {
                    fornecedor.setCategoria(entity.getCategoria());
                }
                if (entity.getStatus() != fornecedor.getStatus()) {
                    fornecedor.setStatus(entity.getStatus());
                }
                if (entity.getStatus() != fornecedor.getStatus()) {
                    fornecedor.setStatus(entity.getStatus());
                }
                fornecedorRepo.save(fornecedor);
                return ResponseEntity.ok("Fornecedor updated successfully");
            }           
            
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fornecedor not found");
    }

    @GetMapping("/gforn")
    public ResponseEntity<FornecedorDTO> getFornecedor(@RequestParam String id) {
        Optional<Fornecedor> fornecedorOpt = fornecedorRepo.findById(id);
            if (fornecedorOpt.isPresent()) {
            Fornecedor fornecedor = fornecedorOpt.get();
    // ...
            FornecedorDTO fornecedorDTO = new FornecedorDTO();
            fornecedorDTO.setId(fornecedor.getId());
            fornecedorDTO.setName(fornecedor.getName());
            fornecedorDTO.setEmail(fornecedor.getEmail());
            fornecedorDTO.setContato(fornecedor.getContato());
            fornecedorDTO.setCategoria(fornecedor.getCategoria());
            fornecedorDTO.setStatus(fornecedor.getStatus());
            return ResponseEntity.ok(fornecedorDTO);
        }
   
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    @GetMapping("/gforns")
    public ResponseEntity<List<FornecedorDTO>> getAllFornecedores() {
        List<Fornecedor> fornecedores = fornecedorRepo.findAll();
        List<FornecedorDTO> fornecedorDTOs = new ArrayList<>();
        for (Fornecedor fornecedor : fornecedores) {
            FornecedorDTO fornecedorDTO = new FornecedorDTO();
            fornecedorDTO.setId(fornecedor.getId());
            fornecedorDTO.setName(fornecedor.getName());
            fornecedorDTO.setEmail(fornecedor.getEmail());
            fornecedorDTO.setContato(fornecedor.getContato());
            fornecedorDTO.setCategoria(fornecedor.getCategoria());
            fornecedorDTO.setStatus(fornecedor.getStatus());
            fornecedorDTOs.add(fornecedorDTO);
        }
        return ResponseEntity.ok(fornecedorDTOs);
    }

    @GetMapping("/dforn")
    public ResponseEntity<String> deleteFornecedor(@RequestParam String id) {
        Optional<Fornecedor> fornecedorOpt = fornecedorRepo.findById(id);
        if (fornecedorOpt.isPresent()) {
            Fornecedor fornecedor = fornecedorOpt.get();
            fornecedor.setStatus(false);
            fornecedorRepo.save(fornecedor);
            return ResponseEntity.ok("Fornecedor deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fornecedor not found");
    }
}
