package puc.airtrack.airtrack.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração para o Azure Blob Storage
 */
@Configuration
public class AzureBlobConfig {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container.name}")
    private String containerName;

    /**
     * Configuração do cliente de serviço Blob do Azure
     */
    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    /**
     * Configuração do cliente de container Blob do Azure
     */
    @Bean
    public BlobContainerClient blobContainerClient(BlobServiceClient blobServiceClient) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            // Cria o container se não existir
            if (!containerClient.exists()) {
                containerClient.create();
                System.out.println("Container criado: " + containerName);
            } else {
                System.out.println("Container já existe: " + containerName);
            }
            return containerClient;
        } catch (Exception e) {
            System.err.println("Erro ao configurar o Azure Blob Storage: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
