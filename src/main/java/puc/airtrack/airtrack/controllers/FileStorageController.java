package puc.airtrack.airtrack.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.BlobClient;

import puc.airtrack.airtrack.services.AzureBlobStorageService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para operações com arquivos no Azure Blob Storage
 */
@RestController
@RequestMapping("/api/files")
public class FileStorageController {

    private final AzureBlobStorageService azureBlobStorageService;

    @Autowired
    public FileStorageController(AzureBlobStorageService azureBlobStorageService) {
        this.azureBlobStorageService = azureBlobStorageService;
    }

    /**
     * Faz upload de um arquivo para o Azure Blob Storage
     * 
     * @param file Arquivo para upload
     * @param prefix Prefixo opcional para o nome do arquivo
     * @return Resposta com URL do arquivo
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prefix", required = false) String prefix) {
        
        try {
            String fileUrl = azureBlobStorageService.uploadFile(file, prefix);
            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Falha ao fazer upload do arquivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Lista todos os arquivos no container
     */
    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles() {
        List<String> fileNames = azureBlobStorageService.listFiles();
        return ResponseEntity.ok(fileNames);
    }

    /**
     * Exclui um arquivo pelo nome
     */
    @DeleteMapping("/{fileName}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName) {
        azureBlobStorageService.deleteFile(fileName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Faz o download de um arquivo diretamente pelo back-end
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName) {
        System.out.println("Iniciando download do arquivo: " + fileName);
        
        // Verifica se o arquivo existe
        if (!azureBlobStorageService.fileExists(fileName)) {
            System.err.println("Erro: Arquivo não encontrado: " + fileName);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Arquivo não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            System.out.println("Arquivo encontrado, iniciando download do Azure Blob Storage");
            
            // Obtém o cliente de blob para o arquivo
            BlobClient blobClient = azureBlobStorageService.getBlobClient(fileName);
            
            // Obtém as propriedades do blob para determinar o tipo de conteúdo
            String contentType = blobClient.getProperties().getContentType();
            if (contentType == null || contentType.isEmpty()) {
                // Se não tiver tipo de conteúdo definido, tenta inferir pelo nome do arquivo
                contentType = inferContentType(fileName);
            }
            System.out.println("Tipo de conteúdo: " + contentType);
            
            // Cria um array de bytes com o conteúdo do arquivo
            byte[] content = blobClient.downloadContent().toBytes();
            System.out.println("Download concluído. Tamanho do arquivo: " + content.length + " bytes");
            
            // Nome do arquivo para o download
            String downloadFilename = extractOriginalFilename(fileName);
            System.out.println("Nome do arquivo para download: " + downloadFilename);
            
            // Configura a resposta HTTP
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + downloadFilename + "\"")
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
            String originalName = fullFileName.substring(secondUnderscoreIndex + 1);
            
            // Trata caracteres especiais no nome do arquivo que podem causar problemas
            originalName = originalName
                    .replaceAll("%20", " ")  // Espaços URL-encoded
                    .replaceAll("%C3%A1", "á")  // á
                    .replaceAll("%C3%A0", "à")  // à
                    .replaceAll("%C3%A2", "â")  // â
                    .replaceAll("%C3%A3", "ã")  // ã
                    .replaceAll("%C3%A9", "é")  // é
                    .replaceAll("%C3%A8", "è")  // è
                    .replaceAll("%C3%AA", "ê")  // ê
                    .replaceAll("%C3%AD", "í")  // í
                    .replaceAll("%C3%B3", "ó")  // ó
                    .replaceAll("%C3%B4", "ô")  // ô
                    .replaceAll("%C3%B5", "õ")  // õ
                    .replaceAll("%C3%BA", "ú")  // ú
                    .replaceAll("%C3%A7", "ç");  // ç
            
            return originalName;
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
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex + 1).toLowerCase();
        }
        
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
}
