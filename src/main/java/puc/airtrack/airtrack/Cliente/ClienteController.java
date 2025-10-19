package puc.airtrack.airtrack.Cliente;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@Tag(name = "Cliente", description = "Gerenciamento de Clientes - Empresas e pessoas que possuem motores cadastrados no sistema")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    @Autowired
    private ClienteRepo clienteRepo;

    @Operation(
        summary = "Cadastrar novo cliente",
        description = "Cria um novo cliente no sistema. O cliente é identificado pelo CPF/CNPJ e pode ser uma empresa de aviação ou pessoa física que possui motores de aeronaves.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do cliente a ser cadastrado",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClienteDTO.class),
                examples = @ExampleObject(
                    name = "Aviação Executiva",
                    value = """
                    {
                      "cpf": "123.456.789-00",
                      "name": "Aviação Executiva Ltda",
                      "email": "contato@aviacaoexecutiva.com.br",
                      "contato": "(11) 98765-4321",
                      "status": true
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Cliente já existe ou dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado - Token inválido ou ausente"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/ccli")
    public ResponseEntity<String> createCliente(@RequestBody ClienteDTO entity) {
        if (entity != null) {
            Optional<Cliente> existingCliente = clienteRepo.findByCpf(entity.getCpf());
            if (existingCliente.isPresent()) {
                return ResponseEntity.badRequest().body("Cliente already exists");
            }
            Cliente cliente = new Cliente();
            cliente.setCpf(entity.getCpf());
            cliente.setName(entity.getName());
            cliente.setEmail(entity.getEmail());
            cliente.setContato(entity.getContato());
            cliente.setStatus(entity.getStatus());
            clienteRepo.save(cliente);
            URI location = URI.create("/gcli?param=" + cliente.getCpf());
            return ResponseEntity.created(location).body("Cliente created successfully");
        }
        return ResponseEntity.badRequest().body("Cliente already exists");
    }

    @Operation(
        summary = "Atualizar dados do cliente",
        description = "Atualiza as informações de um cliente existente identificado pelo CPF/CNPJ. Apenas os campos fornecidos serão atualizados.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados do cliente (CPF é obrigatório para identificação)",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClienteDTO.class),
                examples = @ExampleObject(
                    name = "Atualizar contato",
                    value = """
                    {
                      "cpf": "123.456.789-00",
                      "name": "Aviação Executiva Ltda",
                      "email": "novoemail@aviacaoexecutiva.com.br",
                      "contato": "(11) 91234-5678",
                      "status": true
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PutMapping("/ucli")
    public ResponseEntity<String> updateCliente(@RequestBody ClienteDTO entity) {
        if (entity != null) {
            Optional<Cliente> clienteOpt = clienteRepo.findByCpf(entity.getCpf());
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

    @Operation(
        summary = "Buscar cliente por CPF/CNPJ",
        description = "Retorna os dados completos de um cliente específico identificado pelo CPF ou CNPJ."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cliente encontrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClienteDTO.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "id": 1,
                      "cpf": "123.456.789-00",
                      "name": "Aviação Executiva Ltda",
                      "email": "contato@aviacaoexecutiva.com.br",
                      "contato": "(11) 98765-4321",
                      "status": true
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @GetMapping("/gcli")
    public ResponseEntity<ClienteDTO> getCliente(@RequestParam String id) {
        Optional<Cliente> clienteOpt = clienteRepo.findByCpf(id);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            ClienteDTO clienteDTO = new ClienteDTO();
            clienteDTO.setId(cliente.getId());
            clienteDTO.setCpf(cliente.getCpf());
            clienteDTO.setName(cliente.getName());
            clienteDTO.setEmail(cliente.getEmail());
            clienteDTO.setContato(cliente.getContato());
            clienteDTO.setStatus(cliente.getStatus());
            return ResponseEntity.ok(clienteDTO);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @Operation(
        summary = "Listar todos os clientes",
        description = "Retorna uma lista com todos os clientes cadastrados no sistema, incluindo ativos e inativos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clientes retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClienteDTO.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/gclis")
    public ResponseEntity<List<ClienteDTO>> getAllClientes() {
        List<Cliente> clientes = clienteRepo.findAll();
        List<ClienteDTO> clienteDTOs = new ArrayList<>();
        for (Cliente cliente : clientes) {
            ClienteDTO clienteDTO = new ClienteDTO();
            clienteDTO.setId(cliente.getId());
            clienteDTO.setCpf(cliente.getCpf());
            clienteDTO.setName(cliente.getName());
            clienteDTO.setEmail(cliente.getEmail());
            clienteDTO.setContato(cliente.getContato());
            clienteDTO.setStatus(cliente.getStatus());
            clienteDTOs.add(clienteDTO);
        }
        return ResponseEntity.ok(clienteDTOs);
    }

    @Operation(
        summary = "Desativar cliente",
        description = "Desativa um cliente existente (soft delete). O cliente não é removido do banco de dados, apenas seu status é alterado para inativo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/dcli")
    public ResponseEntity<String> deleteFornecedor(@RequestParam String id) {
        Optional<Cliente> clienteOpt = clienteRepo.findByCpf(id);
        if (clienteOpt.isPresent()) {
            Cliente fornecedor = clienteOpt.get();
            fornecedor.setStatus(false);
            clienteRepo.save(fornecedor);
            return ResponseEntity.ok("Cliente deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente not found");
    }
}