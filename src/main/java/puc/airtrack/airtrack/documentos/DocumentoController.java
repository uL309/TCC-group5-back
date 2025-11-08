package puc.airtrack.airtrack.documentos;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import puc.airtrack.airtrack.services.AzureBlobStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller para gerenciamento de documentos (Manuais)
 */
@Slf4j
@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
@Tag(name = "Documentos", description = "Gerenciamento de manuais e documentos técnicos - Upload para Azure Blob Storage")
@SecurityRequirement(name = "bearerAuth")
public class DocumentoController {

    private final DocumentoService documentoService;
    private final AzureBlobStorageService azureBlobStorageService;

    /**
     * Upload do Manual da Organização de Manutenção (MOM)
     */
    @PostMapping("/mom")
    @Operation(summary = "Upload do Manual da Organização de Manutenção (MOM)", 
               description = "Faz upload do Manual da Organização de Manutenção, substituindo a versão anterior")
    public ResponseEntity<?> uploadManualOrganizacaoManutencao(
            @Parameter(description = "Arquivo do manual (PDF, DOC, DOCX)")
            @RequestParam("arquivo") MultipartFile arquivo,
            @Parameter(description = "Observações sobre esta versão")
            @RequestParam(value = "observacoes", required = false) String observacoes) {
        
        try {
            Documento documento = documentoService.uploadDocumento(
                TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO, arquivo, observacoes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("documento", new DocumentoResponseDTO(documento));
            response.put("mensagem", "Manual da Organização de Manutenção enviado com sucesso");
            
            log.info("Manual da Organização de Manutenção enviado com sucesso. ID: {}", documento.getId());
            return ResponseEntity.ok(response);
            
        } catch (DocumentoException e) {
            log.warn("Erro de validação no upload do Manual da Organização de Manutenção: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (IOException e) {
            log.error("Erro de I/O no upload do Manual da Organização de Manutenção: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro interno do servidor: " + e.getMessage()));
        }
    }

    /**
     * Upload do Manual de Controle da Qualidade (MCQ)
     */
    @PostMapping("/mcq")
    @Operation(summary = "Upload do Manual de Controle da Qualidade (MCQ)", 
               description = "Faz upload do Manual de Controle da Qualidade, substituindo a versão anterior")
    public ResponseEntity<?> uploadManualControleQualidade(
            @Parameter(description = "Arquivo do manual (PDF, DOC, DOCX)")
            @RequestParam("arquivo") MultipartFile arquivo,
            @Parameter(description = "Observações sobre esta versão")
            @RequestParam(value = "observacoes", required = false) String observacoes) {
        
        try {
            Documento documento = documentoService.uploadDocumento(
                TipoDocumento.MANUAL_CONTROLE_QUALIDADE, arquivo, observacoes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("documento", new DocumentoResponseDTO(documento));
            response.put("mensagem", "Manual de Controle da Qualidade enviado com sucesso");
            
            log.info("Manual de Controle da Qualidade enviado com sucesso. ID: {}", documento.getId());
            return ResponseEntity.ok(response);
            
        } catch (DocumentoException e) {
            log.warn("Erro de validação no upload do Manual de Controle da Qualidade: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (IOException e) {
            log.error("Erro de I/O no upload do Manual de Controle da Qualidade: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro interno do servidor: " + e.getMessage()));
        }
    }

    /**
     * Lista todos os documentos ativos
     */
    @GetMapping
    @Operation(summary = "Lista documentos ativos", 
               description = "Retorna todos os documentos ativos disponíveis para os usuários")
    public ResponseEntity<Map<String, Object>> listarDocumentosAtivos() {
        try {
            List<Documento> documentos = documentoService.listarDocumentosAtivos();
            
            Map<String, Object> response = new HashMap<>();
            response.put("documentos", documentos.stream()
                .map(DocumentoResponseDTO::new)
                .toList());
            response.put("total", documentos.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao listar documentos ativos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * Busca documento específico por tipo
     */
    @GetMapping("/{tipo}")
    @Operation(summary = "Busca documento por tipo", 
               description = "Retorna o documento ativo de um tipo específico")
    public ResponseEntity<?> buscarDocumentoPorTipo(
            @Parameter(description = "Tipo do documento")
            @PathVariable TipoDocumento tipo) {
        
        try {
            Optional<Documento> documentoOpt = documentoService.buscarDocumentoAtivo(tipo);
            
            if (documentoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            DocumentoResponseDTO response = new DocumentoResponseDTO(documentoOpt.get());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao buscar documento do tipo {}: {}", tipo, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

    /**
     * Busca Manual da Organização de Manutenção (MOM)
     */
    @GetMapping("/mom")
    @Operation(summary = "Busca Manual da Organização de Manutenção (MOM)", 
               description = "Retorna o Manual da Organização de Manutenção ativo")
    public ResponseEntity<?> buscarMOM() {
        return buscarDocumentoPorTipo(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO);
    }

    /**
     * Busca Manual de Controle da Qualidade (MCQ)
     */
    @GetMapping("/mcq")
    @Operation(summary = "Busca Manual de Controle da Qualidade (MCQ)", 
               description = "Retorna o Manual de Controle da Qualidade ativo")
    public ResponseEntity<?> buscarMCQ() {
        return buscarDocumentoPorTipo(TipoDocumento.MANUAL_CONTROLE_QUALIDADE);
    }

    /**
     * Download de documento
     */
    @GetMapping("/{tipo}/download")
    @Operation(summary = "Download de documento", 
               description = "Faz download do arquivo do documento")
    public ResponseEntity<?> downloadDocumento(
            @Parameter(description = "Tipo do documento")
            @PathVariable TipoDocumento tipo) {
        
        try {
            Optional<Documento> documentoOpt = documentoService.buscarDocumentoAtivo(tipo);
            
            if (documentoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Documento documento = documentoOpt.get();
            
            // Obter stream do arquivo do Azure
            InputStream inputStream = azureBlobStorageService.getBlobClient(documento.getNomeAzure()).openInputStream();
            InputStreamResource resource = new InputStreamResource(inputStream);
            
            // Configurar headers para download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + documento.getNomeOriginal() + "\"");
            headers.setContentType(MediaType.parseMediaType(documento.getTipoConteudo()));
            
            log.info("Download do documento {} iniciado", documento.getNomeOriginal());
            
            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(documento.getTamanhoArquivo())
                .body(resource);
                
        } catch (Exception e) {
            log.error("Erro ao fazer download do documento do tipo {}: {}", tipo, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro ao fazer download do arquivo"));
        }
    }

    /**
     * Download do Manual da Organização de Manutenção (MOM)
     */
    @GetMapping("/mom/download")
    @Operation(summary = "Download do Manual da Organização de Manutenção (MOM)", 
               description = "Faz download do Manual da Organização de Manutenção")
    public ResponseEntity<?> downloadMOM() {
        return downloadDocumento(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO);
    }

    /**
     * Download do Manual de Controle da Qualidade (MCQ)
     */
    @GetMapping("/mcq/download")
    @Operation(summary = "Download do Manual de Controle da Qualidade (MCQ)", 
               description = "Faz download do Manual de Controle da Qualidade")
    public ResponseEntity<?> downloadMCQ() {
        return downloadDocumento(TipoDocumento.MANUAL_CONTROLE_QUALIDADE);
    }

    /**
     * Histórico de versões de um documento
     */
    @GetMapping("/{tipo}/historico")
    @Operation(summary = "Histórico de versões", 
               description = "Retorna o histórico de versões de um tipo de documento")
    public ResponseEntity<?> listarHistoricoDocumento(
            @Parameter(description = "Tipo do documento")
            @PathVariable TipoDocumento tipo) {
        
        try {
            log.info("📋 Buscando histórico para tipo: {}", tipo);
            List<Documento> historico = documentoService.listarHistoricoDocumento(tipo);
            log.info("✅ Encontrados {} documentos no histórico", historico.size());
            
            List<DocumentoResponseDTO> historicoDTO = historico.stream()
                .map(DocumentoResponseDTO::new)
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("tipo", tipo.getDescricao());
            response.put("historico", historicoDTO);
            response.put("total", historico.size());
            
            log.info("📤 Retornando resposta: tipo={}, total={}", tipo.getDescricao(), historico.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Erro ao listar histórico do documento tipo {}: {}", tipo, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro interno do servidor: " + e.getMessage()));
        }
    }

    /**
     * Histórico do Manual da Organização de Manutenção (MOM)
     */
    @GetMapping("/mom/historico")
    @Operation(summary = "Histórico do Manual da Organização de Manutenção (MOM)", 
               description = "Retorna o histórico de versões do Manual da Organização de Manutenção")
    public ResponseEntity<?> listarHistoricoMOM() {
        log.info("📋 Endpoint /mom/historico chamado");
        return listarHistoricoDocumento(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO);
    }

    /**
     * Histórico do Manual de Controle da Qualidade (MCQ)
     */
    @GetMapping("/mcq/historico")
    @Operation(summary = "Histórico do Manual de Controle da Qualidade (MCQ)", 
               description = "Retorna o histórico de versões do Manual de Controle da Qualidade")
    public ResponseEntity<?> listarHistoricoMCQ() {
        log.info("📋 Endpoint /mcq/historico chamado");
        return listarHistoricoDocumento(TipoDocumento.MANUAL_CONTROLE_QUALIDADE);
    }

    /**
     * Remove um documento (apenas admins)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove documento", 
               description = "Remove um documento específico (apenas administradores)")
    public ResponseEntity<?> removerDocumento(
            @Parameter(description = "ID do documento")
            @PathVariable Long id) {
        
        try {
            documentoService.removerDocumento(id);
            log.info("Documento removido com sucesso. ID: {}", id);
            return ResponseEntity.ok(Map.of("mensagem", "Documento removido com sucesso"));
            
        } catch (DocumentoException e) {
            log.warn("Erro ao remover documento ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            log.error("Erro interno ao remover documento ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

}