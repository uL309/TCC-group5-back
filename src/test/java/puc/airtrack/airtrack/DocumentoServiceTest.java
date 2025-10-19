package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import puc.airtrack.airtrack.documentos.Documento;
import puc.airtrack.airtrack.documentos.DocumentoException;
import puc.airtrack.airtrack.documentos.DocumentoRepository;
import puc.airtrack.airtrack.documentos.DocumentoService;
import puc.airtrack.airtrack.documentos.TipoDocumento;
import puc.airtrack.airtrack.services.AzureBlobStorageService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DocumentoServiceTest {

    @InjectMocks
    private DocumentoService service;

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private AzureBlobStorageService azureBlobStorageService;

    private SecurityContext originalContext;

    @BeforeEach
    void before() {
        // preserve original security context and set a test auth
        originalContext = SecurityContextHolder.getContext();
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        // tornar stubs lenient para evitar UnnecessaryStubbingException
        lenient().when(auth.getName()).thenReturn("tester");
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void after() {
        SecurityContextHolder.setContext(originalContext);
    }

    private Documento makeDocumento(TipoDocumento tipo) {
        Documento d = new Documento();
        d.setId(123L);
        d.setTipo(tipo);
        d.setNomeOriginal("manual.pdf");
        d.setNomeAzure("manual_blob.pdf");
        d.setUrlAzure("https://blob/container/manual_blob.pdf");
        d.setTipoConteudo("application/pdf");
        d.setTamanhoArquivo(1024L);
        d.setVersao(1);
        return d;
    }

    @Test
    void uploadDocumento_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("arquivo", "manual.pdf", "application/pdf", "content".getBytes());
        when(documentoRepository.findLatestByTipo(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO)).thenReturn(Optional.empty());
        when(documentoRepository.getNextVersion(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO)).thenReturn(2);
        when(azureBlobStorageService.uploadFile(any(MultipartFile.class), anyString())).thenReturn("https://blob/container/manual_blob.pdf");
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> {
            Documento d = inv.getArgument(0);
            d.setId(11L);
            return d;
        });

        Documento saved = service.uploadDocumento(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO, file, "obs");
        assertNotNull(saved);
        assertEquals(11L, saved.getId());
        assertEquals(2, saved.getVersao());
        assertEquals("manual_blob.pdf", saved.getNomeAzure());
        verify(documentoRepository, never()).desativarTodosDoTipo(any());
    }

    @Test
    void uploadDocumento_existingDocument_desativatesPrevious() throws Exception {
        MockMultipartFile file = new MockMultipartFile("arquivo", "manual.pdf", "application/pdf", "content".getBytes());
        Documento existing = makeDocumento(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO);
        when(documentoRepository.findLatestByTipo(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO)).thenReturn(Optional.of(existing));
        when(documentoRepository.getNextVersion(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO)).thenReturn(3);
        when(azureBlobStorageService.uploadFile(any(MultipartFile.class), anyString())).thenReturn("https://blob/container/new.pdf");
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> {
            Documento d = inv.getArgument(0);
            d.setId(22L);
            return d;
        });

        Documento saved = service.uploadDocumento(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO, file, null);
        assertNotNull(saved);
        verify(documentoRepository).desativarTodosDoTipo(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO);
        assertEquals("new.pdf", saved.getNomeAzure());
    }

    @Test
    void uploadDocumento_nullFile_throws() {
        assertThrows(DocumentoException.class, () ->
            service.uploadDocumento(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO, null, null)
        );
    }

    @Test
    void uploadDocumento_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile("arquivo", "empty.pdf", "application/pdf", new byte[0]);
        assertThrows(DocumentoException.class, () ->
            service.uploadDocumento(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO, file, null)
        );
    }

    @Test
    void uploadDocumento_invalidContentType_throws() {
        MockMultipartFile file = new MockMultipartFile("arquivo", "img.png", "image/png", "x".getBytes());
        assertThrows(DocumentoException.class, () ->
            service.uploadDocumento(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO, file, null)
        );
    }

    @Test
    void uploadDocumento_oversize_throws() {
        MultipartFile big = mock(MultipartFile.class);
        when(big.isEmpty()).thenReturn(false);
        when(big.getSize()).thenReturn(60L * 1024L * 1024L); // 60 MB
        when(big.getContentType()).thenReturn("application/pdf");
        when(big.getOriginalFilename()).thenReturn("big.pdf");

        assertThrows(DocumentoException.class, () ->
            service.uploadDocumento(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO, big, null)
        );
    }

    @Test
    void uploadDocumento_azureIOException_wrapsDocumentoException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("arquivo", "manual.pdf", "application/pdf", "content".getBytes());
        when(documentoRepository.findLatestByTipo(any())).thenReturn(Optional.empty());
        when(azureBlobStorageService.uploadFile(any(MultipartFile.class), anyString())).thenThrow(new IOException("io fail"));

        DocumentoException ex = assertThrows(DocumentoException.class, () ->
            service.uploadDocumento(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO, file, null)
        );
        assertTrue(ex.getMessage().contains("Erro ao fazer upload"));
    }

    @Test
    void buscarDocumentos_listAndSingleAndExists() {
        Documento d = makeDocumento(TipoDocumento.MANUAL_CONTROLE_QUALIDADE);
        when(documentoRepository.findLatestByTipo(TipoDocumento.MANUAL_CONTROLE_QUALIDADE)).thenReturn(Optional.of(d));
        when(documentoRepository.findAllActiveOrderByTipoAndVersao()).thenReturn(List.of(d));
        when(documentoRepository.findAllByTipoOrderByVersaoDesc(TipoDocumento.MANUAL_CONTROLE_QUALIDADE)).thenReturn(List.of(d));
        when(documentoRepository.existsByTipoAndAtivoTrue(TipoDocumento.MANUAL_CONTROLE_QUALIDADE)).thenReturn(true);
        when(documentoRepository.findById(123L)).thenReturn(Optional.of(d));

        Optional<Documento> opt = service.buscarDocumentoAtivo(TipoDocumento.MANUAL_CONTROLE_QUALIDADE);
        assertTrue(opt.isPresent());
        List<Documento> all = service.listarDocumentosAtivos();
        assertEquals(1, all.size());
        List<Documento> hist = service.listarHistoricoDocumento(TipoDocumento.MANUAL_CONTROLE_QUALIDADE);
        assertEquals(1, hist.size());
        assertTrue(service.existeDocumentoAtivo(TipoDocumento.MANUAL_CONTROLE_QUALIDADE));
        Optional<Documento> byId = service.buscarPorId(123L);
        assertTrue(byId.isPresent());
    }

    @Test
    void removerDocumento_success() throws Exception {
        Documento d = makeDocumento(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO);
        when(documentoRepository.findById(5L)).thenReturn(Optional.of(d));
        doNothing().when(azureBlobStorageService).deleteFile(d.getNomeAzure());
        when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> service.removerDocumento(5L));
        verify(azureBlobStorageService).deleteFile(d.getNomeAzure());
        verify(documentoRepository).save(any(Documento.class));
    }

    @Test
    void removerDocumento_notFound_throws() {
        when(documentoRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(DocumentoException.class, () -> service.removerDocumento(9L));
    }

    @Test
    void removerDocumento_deleteFails_throwsDocumentoException() throws Exception {
        Documento d = makeDocumento(TipoDocumento.MANUAL_ORGANIZACAO_MANUTENCAO);
        when(documentoRepository.findById(7L)).thenReturn(Optional.of(d));
        doThrow(new RuntimeException("azure")).when(azureBlobStorageService).deleteFile(d.getNomeAzure());
        DocumentoException ex = assertThrows(DocumentoException.class, () -> service.removerDocumento(7L));
        assertTrue(ex.getMessage().contains("Erro ao remover documento"));
    }
}
