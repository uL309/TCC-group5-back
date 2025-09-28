package puc.airtrack.airtrack.documentos;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import jakarta.servlet.MultipartConfigElement;

/**
 * Configuração para upload de documentos
 */
@Configuration
public class DocumentoConfig {

    // Tamanho máximo para um arquivo: 50MB
    private static final DataSize MAX_FILE_SIZE = DataSize.ofMegabytes(50);
    
    // Tamanho máximo total para a requisição: 60MB
    private static final DataSize MAX_REQUEST_SIZE = DataSize.ofMegabytes(60);

    /**
     * Configuração para multipart files
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Tamanho máximo do arquivo
        factory.setMaxFileSize(MAX_FILE_SIZE);
        
        // Tamanho máximo da requisição
        factory.setMaxRequestSize(MAX_REQUEST_SIZE);
        
        return factory.createMultipartConfig();
    }
}