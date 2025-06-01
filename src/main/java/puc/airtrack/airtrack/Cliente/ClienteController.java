package puc.airtrack.airtrack.Cliente;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ClienteController {

    @Autowired
    private ClienteRepo clienteRepo;

    @PostMapping("/ccli")
    public ResponseEntity<String> createCliente(@RequestBody ClienteDTO entity) {
        if (entity != null) {
            Cliente cliente = new Cliente();
            cliente.setId(entity.getId());
            cliente.setName(entity.getName());
            cliente.setEmail(entity.getEmail());
            cliente.setContato(entity.getContato());
            cliente.setStatus(entity.getStatus());
            clienteRepo.save(cliente);
            URI location = URI.create("/gcli?param=" + cliente.getId());
            return ResponseEntity.created(location).body("Cliente created successfully");
        }
        return ResponseEntity.badRequest().body("Cliente already exists");
    }

    @PutMapping("/ucli")
    public ResponseEntity<String> updateCliente(@RequestBody ClienteDTO entity) {
        if (entity != null) {
            Optional<Cliente> clienteOpt = clienteRepo.findById(entity.getId());
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                if (entity.getId() != null && !entity.getId().equals(cliente.getId())) {
                    cliente.setId(entity.getId());
                }
                if (entity.getName() != null && !entity.getName().equals(cliente.getName())) {
                    cliente.setName(entity.getName());
                }
                if (entity.getEmail() != null && !entity.getEmail().equals(cliente.getEmail())) {
                    cliente.setEmail(entity.getEmail());
                }
                if (entity.getContato() != null && !entity.getContato().equals(cliente.getContato())) {
                    cliente.setContato(entity.getContato());
                }
                if (entity.getStatus() != null && !entity.getStatus().equals(cliente.getStatus())) {
                    cliente.setStatus(entity.getStatus());
                }
                clienteRepo.save(cliente);
                return ResponseEntity.ok("Cliente updated successfully");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente not found");
    }

    @GetMapping("/gcli")
    public ResponseEntity<ClienteDTO> getCliente(@RequestParam String id) {
        Optional<Cliente> clienteOpt = clienteRepo.findById(id);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            ClienteDTO clienteDTO = new ClienteDTO();
            clienteDTO.setId(cliente.getId());
            clienteDTO.setName(cliente.getName());
            clienteDTO.setEmail(cliente.getEmail());
            clienteDTO.setContato(cliente.getContato());
            clienteDTO.setStatus(cliente.getStatus());
            return ResponseEntity.ok(clienteDTO);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/gclis")
    public ResponseEntity<List<ClienteDTO>> getAllClientes() {
        List<Cliente> clientes = clienteRepo.findAll();
        List<ClienteDTO> clienteDTOs = new ArrayList<>();
        for (Cliente cliente : clientes) {
            ClienteDTO clienteDTO = new ClienteDTO();
            clienteDTO.setId(cliente.getId());
            clienteDTO.setName(cliente.getName());
            clienteDTO.setEmail(cliente.getEmail());
            clienteDTO.setContato(cliente.getContato());
            clienteDTO.setStatus(cliente.getStatus());
            clienteDTOs.add(clienteDTO);
        }
        return ResponseEntity.ok(clienteDTOs);
    }

    @GetMapping("/dcli")
    public ResponseEntity<String> deleteFornecedor(@RequestParam String id) {
        Optional<Cliente> clienteOpt = clienteRepo.findById(id);
        if (clienteOpt.isPresent()) {
            Cliente fornecedor = clienteOpt.get();
            fornecedor.setStatus(false);
            clienteRepo.save(fornecedor);
            return ResponseEntity.ok("Cliente deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente not found");
    }
}