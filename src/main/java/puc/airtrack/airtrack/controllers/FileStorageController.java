package puc.airtrack.airtrack.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
     * Obtém a URL de um arquivo pelo nome
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<Map<String, String>> getFileUrl(@PathVariable String fileName) {
        String fileUrl = azureBlobStorageService.getFileUrl(fileName);
        Map<String, String> response = new HashMap<>();
        response.put("fileUrl", fileUrl);
        return ResponseEntity.ok(response);
    }
}
