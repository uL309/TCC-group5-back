package puc.airtrack.airtrack.Fornecedor;

import java.net.URI;
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
@Tag(name = "Fornecedor", description = "Gerenciamento de Fornecedores - Empresas que fornecem peças e componentes para manutenção de motores")
@SecurityRequirement(name = "bearerAuth")
public class FornecedorController {
    
    @Autowired
    private FornecedorRepo fornecedorRepo;

    @Operation(
        summary = "Cadastrar novo fornecedor",
        description = "Cria um novo fornecedor no sistema. Fornecedores são empresas que fornecem peças, componentes e serviços para manutenção de motores de aeronaves.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do fornecedor a ser cadastrado",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FornecedorDTO.class),
                examples = @ExampleObject(
                    name = "Parts Supply Aviation",
                    value = """
                    {
                      "cnpj": "12.345.678/0001-90",
                      "name": "Parts Supply Aviation",
                      "email": "vendas@partssupply.com.br",
                      "contato": "(11) 3456-7890",
                      "categoria": "Peças e Componentes",
                      "status": true
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Fornecedor cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Fornecedor já existe ou dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/cforn")
    public ResponseEntity<String> createFornecedor(@RequestBody FornecedorDTO entity) {
        if (entity != null) {
            Optional<Fornecedor> existingFornecedor = fornecedorRepo.findById(entity.getId());
            if (existingFornecedor.isPresent()) {
                return ResponseEntity.badRequest().body("Fornecedor already exists");
            }
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

    @Operation(
        summary = "Atualizar dados do fornecedor",
        description = "Atualiza as informações de um fornecedor existente identificado pelo CNPJ. Apenas os campos fornecidos serão atualizados.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados do fornecedor (CNPJ é obrigatório)",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FornecedorDTO.class),
                examples = @ExampleObject(
                    name = "Atualizar categoria",
                    value = """
                    {
                      "cnpj": "12.345.678/0001-90",
                      "name": "Parts Supply Aviation",
                      "email": "comercial@partssupply.com.br",
                      "contato": "(11) 3456-7891",
                      "categoria": "Peças Premium",
                      "status": true
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fornecedor atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
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

    @Operation(
        summary = "Buscar fornecedor por CNPJ",
        description = "Retorna os dados completos de um fornecedor específico identificado pelo CNPJ."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Fornecedor encontrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FornecedorDTO.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "cnpj": "12.345.678/0001-90",
                      "name": "Parts Supply Aviation",
                      "email": "vendas@partssupply.com.br",
                      "contato": "(11) 3456-7890",
                      "categoria": "Peças e Componentes",
                      "status": true
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
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
    
    @Operation(
        summary = "Buscar fornecedores por categoria",
        description = "Retorna uma lista de fornecedores filtrados por categoria específica (ex: 'Peças e Componentes', 'Serviços', 'Ferramentas')."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de fornecedores por categoria",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FornecedorDTO.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/gfornc")
    public ResponseEntity<List<FornecedorDTO>> getFornecedorbyCategory(@RequestParam String category) {
        List<Fornecedor> fornecedores = fornecedorRepo.findByCategoria(category);
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

    @Operation(
        summary = "Listar todos os fornecedores",
        description = "Retorna uma lista com todos os fornecedores cadastrados no sistema, incluindo ativos e inativos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de fornecedores retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FornecedorDTO.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
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

    @DeleteMapping("/dforn")
    @Operation(
        summary = "Desativar fornecedor",
        description = "Desativa um fornecedor existente (soft delete). O fornecedor não é removido do banco de dados, apenas seu status é alterado para inativo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fornecedor desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
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
