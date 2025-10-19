package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;

import puc.airtrack.airtrack.services.AzureBlobStorageService;

@ExtendWith(MockitoExtension.class)
public class AzureBlobStorageServiceTest {

    @Mock
    private BlobContainerClient containerClient;

    @Mock
    private BlobClient blobClient;

    @InjectMocks
    private AzureBlobStorageService service;

    @BeforeEach
    void setup() {
        // remover stub global
    }

    @Test
    void uploadFile_success_withoutPrefix() throws Exception {
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("https://blob/container/file.pdf");

        MultipartFile file = new MockMultipartFile("arquivo", "file.pdf", "application/pdf", "content".getBytes());

        String url = service.uploadFile(file);

        assertNotNull(url);
        verify(blobClient).upload(any(ByteArrayInputStream.class), eq(file.getSize()), eq(true));
        ArgumentCaptor<BlobHttpHeaders> headersCaptor = ArgumentCaptor.forClass(BlobHttpHeaders.class);
        verify(blobClient).setHttpHeaders(headersCaptor.capture());
        assertEquals("application/pdf", headersCaptor.getValue().getContentType());
        assertTrue(url.contains("https://"));
    }

    @Test
    void uploadFile_success_withPrefix_generatesPrefixedName() throws Exception {
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("https://blob/container/prefix_xyz_file2.pdf");

        MultipartFile file = new MockMultipartFile("arquivo", "file2.pdf", "application/pdf", "x".getBytes());

        String url = service.uploadFile(file, "prefix");

        assertNotNull(url);
        verify(containerClient).getBlobClient(contains("prefix"));
        verify(blobClient).upload(any(ByteArrayInputStream.class), eq(file.getSize()), eq(true));
    }

    @Test
    void uploadFile_nullOrEmpty_throws() {
        assertThrows(IOException.class, () -> service.uploadFile(null));
        MultipartFile empty = new MockMultipartFile("arquivo", "e.pdf", "application/pdf", new byte[0]);
        assertThrows(IOException.class, () -> service.uploadFile(empty));
    }

    @Test
    void uploadFile_blobThrows_wrappedAsIOException() throws Exception {
        // precisa garantir que o service use o blobClient mockado
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);

        MultipartFile file = new MockMultipartFile("arquivo", "z.pdf", "application/pdf", "b".getBytes());
        doThrow(new RuntimeException("azure fail")).when(blobClient).upload(any(), anyLong(), anyBoolean());

        IOException ex = assertThrows(IOException.class, () -> service.uploadFile(file));
        assertTrue(ex.getMessage().contains("Erro ao fazer upload"));
    }

    @Test
    void listFiles_returnsNames() {
        BlobItem b1 = mock(BlobItem.class);
        BlobItem b2 = mock(BlobItem.class);
        when(b1.getName()).thenReturn("a.txt");
        when(b2.getName()).thenReturn("b.pdf");

        PagedIterable<BlobItem> paged = mock(PagedIterable.class);
        // fazer forEach do mock invocar o Consumer para cada item
        doAnswer(inv -> {
            java.util.function.Consumer<BlobItem> consumer = inv.getArgument(0);
            consumer.accept(b1);
            consumer.accept(b2);
            return null;
        }).when(paged).forEach(any());
        when(containerClient.listBlobs()).thenReturn(paged);

        List<String> files = service.listFiles();
        assertEquals(2, files.size());
        assertTrue(files.contains("a.txt"));
        assertTrue(files.contains("b.pdf"));
    }

    @Test
    void listFilesWithPrefix_filtersByPrefix() {
        BlobItem b1 = mock(BlobItem.class);
        BlobItem b2 = mock(BlobItem.class);
        when(b1.getName()).thenReturn("pref_a.txt");
        when(b2.getName()).thenReturn("other_b.pdf");

        PagedIterable<BlobItem> paged = mock(PagedIterable.class);
        // simula forEach que entrega os itens ao consumer (suficiente para a implementação)
        doAnswer(inv -> {
            java.util.function.Consumer<BlobItem> consumer = inv.getArgument(0);
            consumer.accept(b1);
            consumer.accept(b2);
            return null;
        }).when(paged).forEach(any());

        when(containerClient.listBlobs()).thenReturn(paged);

        List<String> files = service.listFilesWithPrefix("pref_");
        assertEquals(1, files.size());
        assertEquals("pref_a.txt", files.get(0));
    }

    @Test
    void deleteFile_and_getFileUrl_and_exists_delegatesToBlobClient() {
        // garantir que o service receba o blobClient mockado para o nome "x"
        when(containerClient.getBlobClient("x")).thenReturn(blobClient);
        // ou usar anyString() se preferir:
        // when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);

        when(blobClient.getBlobUrl()).thenReturn("https://blob/container/x");
        when(blobClient.exists()).thenReturn(true);

        service.deleteFile("x");
        verify(blobClient).deleteIfExists();

        String url = service.getFileUrl("x");
        assertEquals("https://blob/container/x", url);

        boolean exists = service.fileExists("x");
        assertTrue(exists);
    }
}
