package puc.airtrack.airtrack.Fornecedor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public class FornecedorController {
    
    @Autowired
    private FornecedorRepo fornecedorRepo;

    @GetMapping("/cforn")
    public ResponseEntity<String> getFornecedorById(@RequestBody FornecedorDTO entity) {
        if (entity != null) {
            Fornecedor fornecedor = new Fornecedor();
            URI location;
            fornecedor.setId(entity.getId());
            fornecedor.setName(entity.getName());
            fornecedor.setEmail(entity.getEmail());
            fornecedor.setContato(entity.getContato());
            fornecedor.setCategory(entity.getCategory());
            fornecedor.setStatus(entity.getStatus());
            location = URI.create("/cforn?param=" + entity.getId());
            return ResponseEntity.created(location).body("Fornecedor created successfully");
        }
        return ResponseEntity.badRequest().body("Fornecedor already exists");
    }

    @PostMapping("/uforn")
    public ResponseEntity<String> updateFornecedor(@RequestBody FornecedorDTO entity) {
        if (entity != null) {
            Fornecedor fornecedor = fornecedorRepo.findById(entity.getId());
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
                if (entity.getCategory() != fornecedor.getCategory()) {
                    fornecedor.setCategory(entity.getCategory());
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
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fornecedor not found");
    }

    @GetMapping("/gforn")
    public ResponseEntity<FornecedorDTO> getFornecedor(@RequestParam int id) {
        Fornecedor fornecedor = fornecedorRepo.findById(id);
        if (fornecedor != null) {
            FornecedorDTO fornecedorDTO = new FornecedorDTO();
            fornecedorDTO.setId(fornecedor.getId());
            fornecedorDTO.setName(fornecedor.getName());
            fornecedorDTO.setEmail(fornecedor.getEmail());
            fornecedorDTO.setContato(fornecedor.getContato());
            fornecedorDTO.setCategory(fornecedor.getCategory());
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
            fornecedorDTO.setCategory(fornecedor.getCategory());
            fornecedorDTO.setStatus(fornecedor.getStatus());
            fornecedorDTOs.add(fornecedorDTO);
        }
        return ResponseEntity.ok(fornecedorDTOs);
    }

    @GetMapping("/dforn")
    public ResponseEntity<String> deleteFornecedor(@RequestParam int id) {
        Fornecedor fornecedor = fornecedorRepo.findById(id);
        if (fornecedor != null) {
            fornecedor.setStatus(false);
            fornecedorRepo.save(fornecedor);
            return ResponseEntity.ok("Fornecedor deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fornecedor not found");
    }
}
