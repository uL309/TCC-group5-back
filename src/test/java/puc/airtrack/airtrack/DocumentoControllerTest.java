package puc.airtrack.airtrack;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.azure.storage.blob.BlobClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import puc.airtrack.airtrack.documentos.Documento;
import puc.airtrack.airtrack.documentos.DocumentoController;
import puc.airtrack.airtrack.documentos.DocumentoException;
import puc.airtrack.airtrack.documentos.DocumentoService;
import puc.airtrack.airtrack.documentos.TipoDocumento;

@WebMvcTest(DocumentoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DocumentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentoService documentoService;

    @MockBean
    private puc.airtrack.airtrack.services.AzureBlobStorageService azureBlobStorageService;

    @MockBean
    private puc.airtrack.airtrack.TokenService tokenService;

    @MockBean
    private puc.airtrack.airtrack.SecurityFilter securityFilter;

    private final ObjectMapper mapper = new ObjectMapper();

    private Documento makeDoc() {
        Documento d = new Documento();
        d.setId(11L);
        d.setNomeAzure("blob-name");
        d.setNomeOriginal("manual.pdf");
        d.setTipoConteudo("application/pdf");
        d.setTamanhoArquivo(123L);
        d.setTipo(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO); // <-- evitar NPE
        return d;
    }

    @Test
    void uploadMOM_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("arquivo", "manual.pdf", "application/pdf", "pdfdata".getBytes());
        Documento doc = makeDoc();

        when(documentoService.uploadDocumento(eq(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO), any(), eq("obs")))
            .thenReturn(doc);

        mockMvc.perform(multipart("/api/documentos/mom").file(file).param("observacoes", "obs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensagem").value("Manual da Organização de Manutenção enviado com sucesso"))
            .andExpect(jsonPath("$.documento.id").value(11));
    }

    @Test
    void uploadMOM_documentoException_returnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("arquivo", "manual.pdf", "application/pdf", "pdfdata".getBytes());
        when(documentoService.uploadDocumento(eq(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO), any(), nullable(String.class)))
            .thenThrow(new DocumentoException("invalid"));

        mockMvc.perform(multipart("/api/documentos/mom").file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void uploadMOM_ioException_returnsInternalServerError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("arquivo", "manual.pdf", "application/pdf", "pdfdata".getBytes());
        when(documentoService.uploadDocumento(eq(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO), any(), nullable(String.class)))
            .thenThrow(new IOException("io fail"));

        mockMvc.perform(multipart("/api/documentos/mom").file(file))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void listarDocumentosAtivos_success() throws Exception {
        Documento d = makeDoc();
        when(documentoService.listarDocumentosAtivos()).thenReturn(List.of(d));

        mockMvc.perform(get("/api/documentos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.documentos[0].id").value(11));
    }

    @Test
    void buscarDocumentoPorTipo_notFound() throws Exception {
        when(documentoService.buscarDocumentoAtivo(any(puc.airtrack.airtrack.documentos.TipoDocumento.class)))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/documentos/{tipo}", TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO.name()))
            .andExpect(status().isNotFound());
    }

    @Test
    void buscarDocumentoPorTipo_found() throws Exception {
        Documento d = makeDoc();
        when(documentoService.buscarDocumentoAtivo(TipoDocumento.MANUAL_CONTROLE_QUALIDADE)).thenReturn(Optional.of(d));

        mockMvc.perform(get("/api/documentos/{tipo}", TipoDocumento.MANUAL_CONTROLE_QUALIDADE.name()))
             .andExpect(status().isOk())
             .andExpect(jsonPath("$.id").value(11))
             .andExpect(jsonPath("$.nomeOriginal").value("manual.pdf"));
    }
    
    @Test
    void downloadDocumento_success() throws Exception {
        Documento d = makeDoc();
        when(documentoService.buscarDocumentoAtivo(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO)).thenReturn(Optional.of(d));

        BlobClient blobClient = mock(BlobClient.class);
        com.azure.storage.blob.specialized.BlobInputStream blobInputStream =
            mock(com.azure.storage.blob.specialized.BlobInputStream.class);
        when(azureBlobStorageService.getBlobClient("blob-name")).thenReturn(blobClient);
        when(blobClient.openInputStream()).thenReturn(blobInputStream);

 
        mockMvc.perform(get("/api/documentos/{tipo}/download", TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO.name()))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("manual.pdf")))
            .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void downloadDocumento_notFound() throws Exception {
        when(documentoService.buscarDocumentoAtivo(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/documentos/{tipo}/download", TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO.name()))
            .andExpect(status().isNotFound());
    }

    @Test
    void downloadDocumento_ioError_returnsInternalServerError() throws Exception {
        Documento d = makeDoc();
        when(documentoService.buscarDocumentoAtivo(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO)).thenReturn(Optional.of(d));

        BlobClient blobClient = mock(BlobClient.class);
        when(azureBlobStorageService.getBlobClient("blob-name")).thenReturn(blobClient);
        when(blobClient.openInputStream()).thenThrow(new RuntimeException("io"));

        mockMvc.perform(get("/api/documentos/{tipo}/download", TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO.name()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void listarHistoricoDocumento_success() throws Exception {
        Documento d = makeDoc();
        when(documentoService.listarHistoricoDocumento(TipoDocumento.MANUAL_CONTROLE_QUALIDADE)).thenReturn(List.of(d));

        mockMvc.perform(get("/api/documentos/{tipo}/historico", TipoDocumento.MANUAL_CONTROLE_QUALIDADE.name()))
             .andExpect(status().isOk())
             .andExpect(jsonPath("$.total").value(1))
             .andExpect(jsonPath("$.historico[0].id").value(11));
    }

    @Test
    void removerDocumento_success_and_errors() throws Exception {
        // success
        doNothing().when(documentoService).removerDocumento(5L);
        mockMvc.perform(delete("/api/documentos/{id}", 5L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensagem").exists());

        // DocumentoException -> bad request
        doThrow(new DocumentoException("cannot")).when(documentoService).removerDocumento(6L);
        mockMvc.perform(delete("/api/documentos/{id}", 6L))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").exists());

        // runtime exception -> internal server error
        doThrow(new RuntimeException("boom")).when(documentoService).removerDocumento(7L);
        mockMvc.perform(delete("/api/documentos/{id}", 7L))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void uploadMCQ_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("arquivo","mcq.pdf","application/pdf","pdf".getBytes());
        Documento doc = makeDoc();
        when(documentoService.uploadDocumento(eq(TipoDocumento.MANUAL_CONTROLE_QUALIDADE), any(), eq("obs")))
            .thenReturn(doc);

        mockMvc.perform(multipart("/api/documentos/mcq").file(file).param("observacoes","obs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensagem").exists())
            .andExpect(jsonPath("$.documento.id").value(11));
    }

    @Test
    void buscarMOM_delegatesToBuscarDocumentoPorTipo() throws Exception {
        Documento d = makeDoc();
        when(documentoService.buscarDocumentoAtivo(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO)).thenReturn(Optional.of(d));
        mockMvc.perform(get("/api/documentos/mom"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(11));
    }

    @Test
    void buscarMCQ_delegatesToBuscarDocumentoPorTipo() throws Exception {
        Documento d = makeDoc();
        when(documentoService.buscarDocumentoAtivo(TipoDocumento.MANUAL_CONTROLE_QUALIDADE)).thenReturn(Optional.of(d));
        mockMvc.perform(get("/api/documentos/mcq"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(11));
    }
}
