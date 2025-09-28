package puc.airtrack.airtrack.documentos;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import puc.airtrack.airtrack.services.AzureBlobStorageService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Serviço para gerenciamento de documentos (Manuais)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final AzureBlobStorageService azureBlobStorageService;

    // Tipos de arquivo permitidos
    private static final Set<String> TIPOS_ARQUIVO_PERMITIDOS = Set.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    // Tamanho máximo do arquivo: 50MB
    private static final long TAMANHO_MAXIMO_ARQUIVO = 50 * 1024 * 1024;

    /**
     * Faz upload de um documento, substituindo o anterior se existir
     */
    @Transactional
    public Documento uploadDocumento(TipoDocumento tipo, MultipartFile arquivo, String observacoes) 
            throws IOException, DocumentoException {
        
        log.info("Iniciando upload de documento do tipo: {}", tipo);
        
        // Validações
        validarArquivo(arquivo);
        
        // Obter usuário atual
        String usuarioAtual = obterUsuarioAtual();
        
        // Desativar documento anterior se existir
        Optional<Documento> documentoExistente = documentoRepository.findLatestByTipo(tipo);
        if (documentoExistente.isPresent()) {
            log.info("Documento existente encontrado para o tipo {}, desativando...", tipo);
            documentoRepository.desativarTodosDoTipo(tipo);
        }
        
        // Upload para Azure
        String prefixo = tipo.getSigla().toLowerCase() + "_v";
        String urlAzure;
        try {
            urlAzure = azureBlobStorageService.uploadFile(arquivo, prefixo);
            log.info("Upload para Azure concluído. URL: {}", urlAzure);
        } catch (IOException e) {
            log.error("Erro no upload para Azure: {}", e.getMessage());
            throw new DocumentoException("Erro ao fazer upload do arquivo para o Azure: " + e.getMessage());
        }
        
        // Extrair nome do arquivo no Azure da URL
        String nomeAzure = extrairNomeArquivoAzure(urlAzure);
        
        // Obter próxima versão
        Integer proximaVersao = documentoRepository.getNextVersion(tipo);
        
        // Criar novo documento
        Documento novoDocumento = new Documento(
            tipo,
            arquivo.getOriginalFilename(),
            nomeAzure,
            urlAzure,
            arquivo.getSize(),
            arquivo.getContentType(),
            usuarioAtual
        );
        novoDocumento.setVersao(proximaVersao);
        novoDocumento.setObservacoes(observacoes);
        
        // Salvar no banco
        Documento documentoSalvo = documentoRepository.save(novoDocumento);
        log.info("Documento salvo com sucesso. ID: {}, Versão: {}", documentoSalvo.getId(), documentoSalvo.getVersao());
        
        return documentoSalvo;
    }

    /**
     * Busca o documento ativo de um tipo específico
     */
    public Optional<Documento> buscarDocumentoAtivo(TipoDocumento tipo) {
        log.debug("Buscando documento ativo do tipo: {}", tipo);
        return documentoRepository.findLatestByTipo(tipo);
    }

    /**
     * Lista todos os documentos ativos
     */
    public List<Documento> listarDocumentosAtivos() {
        log.debug("Listando todos os documentos ativos");
        return documentoRepository.findAllActiveOrderByTipoAndVersao();
    }

    /**
     * Lista histórico de versões de um tipo de documento
     */
    public List<Documento> listarHistoricoDocumento(TipoDocumento tipo) {
        log.debug("Listando histórico do documento tipo: {}", tipo);
        return documentoRepository.findAllByTipoOrderByVersaoDesc(tipo);
    }

    /**
     * Remove um documento (desativa e exclui do Azure)
     */
    @Transactional
    public void removerDocumento(Long documentoId) throws DocumentoException {
        log.info("Removendo documento ID: {}", documentoId);
        
        Optional<Documento> documentoOpt = documentoRepository.findById(documentoId);
        if (documentoOpt.isEmpty()) {
            throw new DocumentoException("Documento não encontrado");
        }
        
        Documento documento = documentoOpt.get();
        
        try {
            // Excluir do Azure
            azureBlobStorageService.deleteFile(documento.getNomeAzure());
            log.info("Arquivo excluído do Azure: {}", documento.getNomeAzure());
            
            // Desativar no banco
            documento.desativar();
            documentoRepository.save(documento);
            log.info("Documento desativado no banco de dados");
            
        } catch (Exception e) {
            log.error("Erro ao remover documento: {}", e.getMessage());
            throw new DocumentoException("Erro ao remover documento: " + e.getMessage());
        }
    }

    /**
     * Verifica se existe documento ativo de um tipo
     */
    public boolean existeDocumentoAtivo(TipoDocumento tipo) {
        return documentoRepository.existsByTipoAndAtivoTrue(tipo);
    }

    /**
     * Busca documento por ID
     */
    public Optional<Documento> buscarPorId(Long id) {
        return documentoRepository.findById(id);
    }

    /**
     * Valida o arquivo antes do upload
     */
    private void validarArquivo(MultipartFile arquivo) throws DocumentoException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new DocumentoException("Arquivo é obrigatório");
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO_ARQUIVO) {
            throw new DocumentoException(
                String.format("Arquivo muito grande. Tamanho máximo permitido: %.1f MB", 
                    TAMANHO_MAXIMO_ARQUIVO / (1024.0 * 1024.0))
            );
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !TIPOS_ARQUIVO_PERMITIDOS.contains(contentType)) {
            throw new DocumentoException(
                "Tipo de arquivo não permitido. Tipos aceitos: PDF, DOC, DOCX"
            );
        }

        String nomeArquivo = arquivo.getOriginalFilename();
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            throw new DocumentoException("Nome do arquivo é obrigatório");
        }
    }

    /**
     * Obtém o usuário atual do contexto de segurança
     */
    private String obterUsuarioAtual() {
        try {
            String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
            return usuario != null ? usuario : "sistema";
        } catch (Exception e) {
            log.warn("Não foi possível obter o usuário atual: {}", e.getMessage());
            return "sistema";
        }
    }

    /**
     * Extrai o nome do arquivo do Azure a partir da URL
     */
    private String extrairNomeArquivoAzure(String urlAzure) {
        try {
            return urlAzure.substring(urlAzure.lastIndexOf("/") + 1);
        } catch (Exception e) {
            log.warn("Erro ao extrair nome do arquivo da URL: {}", urlAzure);
            return "arquivo_" + System.currentTimeMillis();
        }
    }
}