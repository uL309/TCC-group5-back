package puc.airtrack.airtrack.Pecas;

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
import puc.airtrack.airtrack.Fornecedor.Fornecedor;
import puc.airtrack.airtrack.Fornecedor.FornecedorRepo;


@RestController
@Tag(name = "Peças", description = "Gerenciamento de Peças e Componentes - Estoque de peças para manutenção de motores de aeronaves")
@SecurityRequirement(name = "bearerAuth")
public class PecasController {

    @Autowired
    private PecasRepository pecasRepository;

    @Autowired
    private FornecedorRepo fornecedorRepository;

    @Operation(
        summary = "Cadastrar nova peça",
        description = "Adiciona uma nova peça ou componente ao estoque. A peça pode ser vinculada a um fornecedor e a um engenheiro responsável.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da peça a ser cadastrada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PecasDTO.class),
                examples = @ExampleObject(
                    name = "Filtro de Óleo",
                    value = """
                    {
                      "nome": "Filtro de Óleo PT6A",
                      "num_serie": "FLT-PT6A-001",
                      "data_aquisicao": "2025-10-15",
                      "status": true,
                      "categoria": "Filtros",
                      "valor": 450.00,
                      "id_engenheiro": 1,
                      "fornecedor": "12.345.678/0001-90"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Peça cadastrada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/cpeca")
    public ResponseEntity<String> createPeca(@RequestBody PecasDTO entity) {
        if (entity != null) {
            Pecas pecas = new Pecas();
            URI location;
            pecas.setNome(entity.getNome());
            pecas.setNum_serie(entity.getNumSerie());
            pecas.setData_aquisicao(entity.getDataAquisicao());
            pecas.setStatus(entity.getStatus());
            pecas.setCategoria(entity.getCategoria());
            pecas.setValor(entity.getValor());
            pecas.setId_engenheiro(entity.getId_engenheiro());
            if (entity.getFornecedorId() != null) {
                Optional<Fornecedor> fornecedor = fornecedorRepository.findById(entity.getFornecedorId());
                fornecedor.ifPresent(pecas::setFornecedor);
            }
            pecasRepository.save(pecas);
            location = URI.create("/gpeca?param=" + pecas.getId());
            return ResponseEntity.created(location).body("Peca created successfully");
        }
        return ResponseEntity.badRequest().body("Peca already exists");
    }

    @Operation(
        summary = "Atualizar peça",
        description = "Atualiza os dados de uma peça existente no estoque.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da peça (ID é obrigatório)",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PecasDTO.class),
                examples = @ExampleObject(
                    name = "Atualizar valor",
                    value = """
                    {
                      "id": 1,
                      "nome": "Filtro de Óleo PT6A",
                      "num_serie": "FLT-PT6A-001",
                      "data_aquisicao": "2025-10-15",
                      "status": true,
                      "categoria": "Filtros",
                      "valor": 475.00,
                      "id_engenheiro": 1,
                      "fornecedor": "12.345.678/0001-90"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Peça atualizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Peça não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PutMapping("/upeca")
    public ResponseEntity<String> updatePeca(@RequestBody PecasDTO entity) {
        if (entity != null) {
            Optional<Pecas> optionalPecas = pecasRepository.findById(entity.getId());
            if (optionalPecas.isPresent()) {
                Pecas pecas = optionalPecas.get();
                pecas.setNome(entity.getNome());
                pecas.setNum_serie(entity.getNumSerie());
                pecas.setData_aquisicao(entity.getDataAquisicao());
                pecas.setStatus(entity.getStatus());
                pecas.setCategoria(entity.getCategoria());
                pecas.setValor(entity.getValor());
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

    @Operation(
        summary = "Buscar peça por ID",
        description = "Retorna os dados completos de uma peça específica identificada pelo ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Peça encontrada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PecasDTO.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "id": 1,
                      "nome": "Filtro de Óleo PT6A",
                      "num_serie": "FLT-PT6A-001",
                      "data_aquisicao": "2025-10-15",
                      "status": true,
                      "categoria": "Filtros",
                      "valor": 450.00,
                      "id_engenheiro": 1,
                      "fornecedor": "12.345.678/0001-90",
                      "fornecedor_nome": "Parts Supply Aviation"
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Peça não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @GetMapping("/gpeca")
    public ResponseEntity<PecasDTO> getPeca(@RequestParam int id) {
        Optional<Pecas> optionalPecas = pecasRepository.findById(id);
        if (optionalPecas.isPresent()) {
            Pecas pecas = optionalPecas.get();
            PecasDTO dto = new PecasDTO();
            dto.setId(pecas.getId());
            dto.setNome(pecas.getNome());
            dto.setNumSerie(pecas.getNum_serie());
            dto.setDataAquisicao(pecas.getData_aquisicao());
            dto.setStatus(pecas.getStatus());
            dto.setCategoria(pecas.getCategoria());
            dto.setId_engenheiro(pecas.getId_engenheiro());
            dto.setValor(pecas.getValor());
            dto.setFornecedorId(pecas.getFornecedor() != null ? pecas.getFornecedor().getId() : null);
            dto.setFornecedorNome(pecas.getFornecedor() != null ? pecas.getFornecedor().getName() : null);
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @Operation(
        summary = "Listar todas as peças",
        description = "Retorna uma lista com todas as peças cadastradas no estoque, incluindo informações do fornecedor."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de peças retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PecasDTO.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/gpecas")
    public ResponseEntity<List<PecasDTO>> getAllPecas() {
        List<Pecas> pecasList = pecasRepository.findAll();
        List<PecasDTO> pecasDTOs = new ArrayList<>();
        for (Pecas pecas : pecasList) {
            PecasDTO dto = new PecasDTO();
            dto.setId(pecas.getId());
            dto.setNome(pecas.getNome());
            dto.setNumSerie(pecas.getNum_serie());
            dto.setDataAquisicao(pecas.getData_aquisicao());
            dto.setStatus(pecas.getStatus());
            dto.setCategoria(pecas.getCategoria());
            dto.setId_engenheiro(pecas.getId_engenheiro());
            dto.setValor(pecas.getValor());
            dto.setFornecedorId(pecas.getFornecedor() != null ? pecas.getFornecedor().getId() : null);
            dto.setFornecedorNome(pecas.getFornecedor() != null ? pecas.getFornecedor().getName() : null);
            pecasDTOs.add(dto);
        }
        return ResponseEntity.ok(pecasDTOs);
    }

    @Operation(
        summary = "Desativar peça",
        description = "Desativa uma peça do estoque (soft delete). A peça não é removida do banco de dados, apenas seu status é alterado para inativo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Peça desativada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Peça não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/dpeca")
    public ResponseEntity<String> deletePeca(@RequestParam int id) {
        Optional<Pecas> optionalPecas = pecasRepository.findById(id);
        if (optionalPecas.isPresent()) {
            Pecas pecas = optionalPecas.get();
            pecas.setStatus(false);
            pecasRepository.save(pecas);
            return ResponseEntity.ok("Peca deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Peca not found");
    }
}