package puc.airtrack.airtrack.documentos;

/**
 * Exceção específica para operações de documentos
 */
public class DocumentoException extends Exception {

    public DocumentoException(String message) {
        super(message);
    }

    public DocumentoException(String message, Throwable cause) {
        super(message, cause);
    }
}