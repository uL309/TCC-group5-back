package puc.airtrack.airtrack.documentos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.Map;

/**
 * Handler para exceções relacionadas a documentos
 */
@Slf4j
@ControllerAdvice
public class DocumentoExceptionHandler {

    @ExceptionHandler(DocumentoException.class)
    public ResponseEntity<Map<String, String>> handleDocumentoException(DocumentoException e) {
        log.warn("Erro de validação de documento: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("Arquivo muito grande para upload: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(Map.of("erro", "Arquivo muito grande. Tamanho máximo permitido: 50MB"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, String>> handleMultipartException(MultipartException e) {
        log.warn("Erro no upload de arquivo multipart: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(Map.of("erro", "Erro no upload do arquivo. Verifique se o arquivo não está corrompido."));
    }
}