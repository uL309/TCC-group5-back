package puc.airtrack.airtrack.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Serviço para operações com o Azure Blob Storage
 */
@Service
public class AzureBlobStorageService {

    private final BlobContainerClient containerClient;

    public AzureBlobStorageService(BlobContainerClient containerClient) {
        this.containerClient = containerClient;
    }

    /**
     * Faz upload de um arquivo para o Azure Blob Storage
     * 
     * @param file Arquivo para upload
     * @param prefix Prefixo para o nome do arquivo (opcional)
     * @return URL do arquivo no Azure Blob Storage
     */
    public String uploadFile(MultipartFile file, String prefix) throws IOException {
        // Verificar se o arquivo está vazio
        if (file == null || file.isEmpty()) {
            throw new IOException("Arquivo vazio ou nulo");
        }

        try {
            // Gerar um nome único para o arquivo
            String fileName = (prefix != null && !prefix.isEmpty() ? prefix + "_" : "") 
                    + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            
            // Obter referência do blob
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            
            // Configurar cabeçalhos HTTP (tipo de conteúdo)
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType());
            
            // Fazer upload do arquivo
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);
            
            // Retornar a URL do arquivo
            return blobClient.getBlobUrl();
        } catch (Exception e) {
            throw new IOException("Erro ao fazer upload para o Azure Blob Storage: " + e.getMessage(), e);
        }
    }
    
    /**
     * Faz upload de um arquivo para o Azure Blob Storage (sem prefixo)
     */
    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, null);
    }
    
    /**
     * Lista os nomes de todos os arquivos no container
     */
    public List<String> listFiles() {
        List<String> fileNames = new ArrayList<>();
        containerClient.listBlobs().forEach(blobItem -> {
            fileNames.add(blobItem.getName());
        });
        return fileNames;
    }
    
    /**
     * Exclui um arquivo pelo nome
     */
    public void deleteFile(String fileName) {
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        blobClient.deleteIfExists();
    }
    
    /**
     * Obtém a URL de um arquivo pelo nome
     */
    public String getFileUrl(String fileName) {
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        return blobClient.getBlobUrl();
    }
    
    /**
     * Obtém o BlobClient para um arquivo pelo nome
     */
    public BlobClient getBlobClient(String fileName) {
        return containerClient.getBlobClient(fileName);
    }
    
    /**
     * Verifica se um arquivo existe pelo nome
     */
    public boolean fileExists(String fileName) {
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        return blobClient.exists();
    }
    
    /**
     * Lista todos os arquivos com um determinado prefixo
     */
    public List<String> listFilesWithPrefix(String prefix) {
        List<String> fileNames = new ArrayList<>();
        containerClient.listBlobs().forEach(blobItem -> {
            if (blobItem.getName().startsWith(prefix)) {
                fileNames.add(blobItem.getName());
            }
        });
        return fileNames;
    }
}
