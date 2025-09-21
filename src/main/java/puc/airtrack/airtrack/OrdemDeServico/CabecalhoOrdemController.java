package puc.airtrack.airtrack.OrdemDeServico;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.BlobClient;

import puc.airtrack.airtrack.services.AzureBlobStorageService;
import puc.airtrack.airtrack.services.OrdemServicoPdfService;

@RestController
@RequestMapping("/ordem")
public class CabecalhoOrdemController {
    @Autowired
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;
    @Autowired
    private LinhaOrdemService linhaOrdemService;
    @Autowired
    private CabecalhoOrdemService cabecalhoOrdemService;
    @Autowired
    private AzureBlobStorageService azureBlobStorageService;
    @Autowired
    private OrdemServicoPdfService ordemServicoPdfService;

    @PostMapping("/create")
    public ResponseEntity<String> createCabecalho(@RequestBody CabecalhoOrdemDTO dto) {
        return cabecalhoOrdemService.createCabecalho(dto);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateCabecalho(@RequestBody CabecalhoOrdemDTO dto) {
        return cabecalhoOrdemService.updateCabecalho(dto);
    }

    @GetMapping("/get")
    public ResponseEntity<CabecalhoOrdemDTO> getCabecalho(@RequestParam int id) {
        Optional<CabecalhoOrdem> opt = cabecalhoOrdemRepository.findById(id);
        if (opt.isPresent()) {
            CabecalhoOrdem entity = opt.get();
            CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
            dto.setId(entity.getId());
            dto.setDataAbertura(entity.getDataAbertura());
            dto.setDataFechamento(entity.getDataFechamento());
            dto.setDescricao(entity.getDescricao());
            dto.setTipo(entity.getTipo());
            dto.setTempoUsado(entity.getTempoUsado());
            dto.setTempoEstimado(entity.getTempoEstimado());
            dto.setStatus(entity.getStatus().getStatus());
            dto.setValorHora(entity.getValorHora());
            if (entity.getCliente() != null) {
                dto.setClienteId(entity.getCliente().getCpf());
                dto.setClienteNome(entity.getCliente().getName());
            }
            if (entity.getNumSerieMotor() != null) {
                dto.setMotorId(String.valueOf(entity.getNumSerieMotor().getId()));
                dto.setMotorNome(entity.getNumSerieMotor().getSerie_motor());
                dto.setHorasOperacaoMotor(entity.getNumSerieMotor().getHoras_operacao());
            }
            if (entity.getSupervisor() != null) {
                dto.setSupervisorId(String.valueOf(entity.getSupervisor().getId()));
                dto.setSupervisorNome(entity.getSupervisor().getName());
            }

            if (entity.getEngenheiroAtuante() != null) {
                dto.setEngenheiroAtuanteId(String.valueOf(entity.getEngenheiroAtuante().getId()));
                dto.setEngenheiroAtuanteNome(entity.getEngenheiroAtuante().getName());
            }

            dto.setLinhas(linhaOrdemService.findByCabecalhoId(id));
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/list")
    public ResponseEntity<List<CabecalhoOrdemDTO>> getAllCabecalhos() {
        List<CabecalhoOrdem> list = cabecalhoOrdemRepository.findAllByOrderByIdDesc();
        List<CabecalhoOrdemDTO> dtos = new ArrayList<>();
        for (CabecalhoOrdem entity : list) {
            CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
            dto.setId(entity.getId());
            dto.setDataAbertura(entity.getDataAbertura());
            dto.setDataFechamento(entity.getDataFechamento());
            dto.setDescricao(entity.getDescricao());
            dto.setTipo(entity.getTipo());
            dto.setTempoUsado(entity.getTempoUsado());
            dto.setTempoEstimado(entity.getTempoEstimado());
            dto.setStatus(entity.getStatus().getStatus());
            dto.setValorHora(entity.getValorHora());
            if (entity.getCliente() != null) {
                dto.setClienteId(entity.getCliente().getCpf());
                dto.setClienteNome(entity.getCliente().getName());
            }
            if (entity.getNumSerieMotor() != null) {
                dto.setMotorId(String.valueOf(entity.getNumSerieMotor().getId()));
                dto.setMotorNome(entity.getNumSerieMotor().getSerie_motor());
            }
            if (entity.getSupervisor() != null) {
                dto.setSupervisorId(String.valueOf(entity.getSupervisor().getId()));
                dto.setSupervisorNome(entity.getSupervisor().getName());
            }
            System.out.println("Adding CabecalhoOrdemDTO: " + dto);
            dtos.add(dto);
        }
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCabecalho(@RequestParam int id) {
        Optional<CabecalhoOrdem> opt = cabecalhoOrdemRepository.findById(id);
        if (opt.isPresent()) {
            cabecalhoOrdemRepository.deleteById(id);
            return ResponseEntity.ok("CabecalhoOrdem deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CabecalhoOrdem not found");
    }

    @PutMapping("/atualizar-status")
    public ResponseEntity<String> atualizarStatus(
            @RequestParam int cabecalhoId,
            @RequestParam int status) {
        return cabecalhoOrdemService.atualizarStatusCabecalho(cabecalhoId, status);
    }
    
    /**
     * Faz upload de um anexo para uma ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @param file Arquivo a ser anexado
     * @return URL do arquivo no Azure Blob Storage
     */
    @PostMapping("/{cabecalhoId}/anexos")
    public ResponseEntity<Map<String, String>> uploadAnexo(
            @PathVariable int cabecalhoId,
            @RequestParam("file") MultipartFile file) {
        
        System.out.println("Iniciando upload de anexo para ordem: " + cabecalhoId);
        System.out.println("Nome do arquivo: " + file.getOriginalFilename());
        System.out.println("Tamanho do arquivo: " + file.getSize() + " bytes");
        
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            System.out.println("Erro: Ordem de serviço não encontrada - ID: " + cabecalhoId);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            // Usa o ID da ordem como prefixo para organização dos arquivos
            String prefix = "ordem_" + cabecalhoId;
            System.out.println("Iniciando upload para Azure Blob Storage com prefixo: " + prefix);
            
            String fileUrl = azureBlobStorageService.uploadFile(file, prefix);
            System.out.println("Upload concluído com sucesso. URL: " + fileUrl);
            
            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            response.put("message", "Anexo adicionado com sucesso à ordem de serviço " + cabecalhoId);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.err.println("Erro durante o upload: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Falha ao fazer upload do anexo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lista todos os anexos de uma ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @return Lista de nomes de arquivos anexados à ordem
     */
    @GetMapping("/{cabecalhoId}/anexos")
    public ResponseEntity<?> listarAnexos(@PathVariable int cabecalhoId) {
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        String prefix = "ordem_" + cabecalhoId;
        List<String> arquivos = azureBlobStorageService.listFilesWithPrefix(prefix);
        
        List<Map<String, String>> resultados = new ArrayList<>();
        for (String nomeArquivo : arquivos) {
            Map<String, String> arquivoInfo = new HashMap<>();
            arquivoInfo.put("nome", nomeArquivo);
            arquivoInfo.put("url", azureBlobStorageService.getFileUrl(nomeArquivo));
            resultados.add(arquivoInfo);
        }
        
        return ResponseEntity.ok(resultados);
    }
    
    /**
     * Faz o download de um anexo específico de uma ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @param nomeArquivo Nome do arquivo a ser baixado
     * @return Arquivo para download
     */
    @GetMapping("/{cabecalhoId}/anexos/{nomeArquivo}")
    public ResponseEntity<?> downloadAnexo(
            @PathVariable int cabecalhoId,
            @PathVariable String nomeArquivo) {
        
        System.out.println("Iniciando download de anexo: " + nomeArquivo + " da ordem: " + cabecalhoId);
        
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Verifica se o arquivo existe
        if (!azureBlobStorageService.fileExists(nomeArquivo)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Arquivo não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            // Obtém o cliente de blob para o arquivo
            BlobClient blobClient = azureBlobStorageService.getBlobClient(nomeArquivo);
            
            // Obtém as propriedades do blob para determinar o tipo de conteúdo
            String contentType = blobClient.getProperties().getContentType();
            if (contentType == null || contentType.isEmpty()) {
                // Se não tiver tipo de conteúdo definido, tenta inferir pelo nome do arquivo
                contentType = inferContentType(nomeArquivo);
            }
            
            // Cria um array de bytes com o conteúdo do arquivo
            byte[] content = blobClient.downloadContent().toBytes();
            
            // Configura a resposta HTTP
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + extractOriginalFilename(nomeArquivo) + "\"")
                    .body(content);
            
        } catch (Exception e) {
            System.err.println("Erro ao fazer download do arquivo: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao processar o download: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Extrai o nome original do arquivo do nome completo armazenado no Azure
     * 
     * @param fullFileName Nome completo do arquivo (com prefixo e UUID)
     * @return Nome original do arquivo
     */
    private String extractOriginalFilename(String fullFileName) {
        // Os nomes dos arquivos estão no formato: prefixo_UUID_nomeOriginal
        // Vamos pegar tudo após o segundo underscore
        int secondUnderscoreIndex = fullFileName.indexOf('_', fullFileName.indexOf('_') + 1);
        if (secondUnderscoreIndex > 0 && secondUnderscoreIndex < fullFileName.length() - 1) {
            return fullFileName.substring(secondUnderscoreIndex + 1);
        }
        return fullFileName; // Fallback para o nome completo
    }
    
    /**
     * Infere o tipo de conteúdo com base na extensão do arquivo
     * 
     * @param fileName Nome do arquivo
     * @return Tipo de conteúdo MIME
     */
    private String inferContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }
    
    /**
     * Exclui um anexo específico de uma ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @param nomeArquivo Nome do arquivo a ser excluído
     * @return Mensagem de confirmação
     */
    @DeleteMapping("/{cabecalhoId}/anexos/{nomeArquivo}")
    public ResponseEntity<?> excluirAnexo(
            @PathVariable int cabecalhoId,
            @PathVariable String nomeArquivo) {
        
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Verifica se o arquivo existe
        if (!azureBlobStorageService.fileExists(nomeArquivo)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Arquivo não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Exclui o arquivo
        azureBlobStorageService.deleteFile(nomeArquivo);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Arquivo excluído com sucesso");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gera um PDF com todos os detalhes da ordem de serviço
     * 
     * @param cabecalhoId ID da ordem de serviço
     * @return Arquivo PDF para download
     */
    @GetMapping("/{cabecalhoId}/pdf")
    public ResponseEntity<?> gerarPdfOrdemServico(@PathVariable int cabecalhoId) {
        System.out.println("Iniciando geração de PDF para ordem: " + cabecalhoId);
        
        Optional<CabecalhoOrdem> optCabecalho = cabecalhoOrdemRepository.findById(cabecalhoId);
        if (optCabecalho.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ordem de serviço não encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            // Gerar o PDF usando o serviço
            byte[] pdfBytes = ordemServicoPdfService.gerarPdfOrdemServico(cabecalhoId);
            
            // Nome do arquivo para download
            String fileName = "ordem_servico_" + cabecalhoId + ".pdf";
            
            // Retornar o PDF como download
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + fileName + "\"")
                    .body(pdfBytes);
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar PDF: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao gerar PDF da ordem de serviço: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
