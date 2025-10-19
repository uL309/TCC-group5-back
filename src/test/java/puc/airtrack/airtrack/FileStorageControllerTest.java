package puc.airtrack.airtrack;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobProperties;

import puc.airtrack.airtrack.SecurityFilter;
import puc.airtrack.airtrack.controllers.FileStorageController;
import puc.airtrack.airtrack.services.AzureBlobStorageService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = FileStorageController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FileStorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AzureBlobStorageService azureBlobStorageService;

    @MockBean
    private puc.airtrack.airtrack.TokenService tokenService;

    // se sua aplicação registra um filtro chamado "securityFilter", forneça um mock como Filter
    @MockBean(name = "securityFilter")
    private SecurityFilter securityFilter;

    @MockBean
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void uploadFile_success_returnsUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        when(azureBlobStorageService.uploadFile(any(), isNull())).thenReturn("https://blob/container/test.pdf");

        mockMvc.perform(multipart("/api/files/upload").file(file))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.fileUrl").value("https://blob/container/test.pdf"));

        verify(azureBlobStorageService).uploadFile(any(), isNull());
    }

    @Test
    void uploadFile_withPrefix_success_returnsUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "f.pdf", "application/pdf", "b".getBytes());
        when(azureBlobStorageService.uploadFile(any(), eq("pref"))).thenReturn("https://blob/container/pref_x_f.pdf");

        mockMvc.perform(multipart("/api/files/upload").file(file).param("prefix", "pref"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.fileUrl").exists());

        verify(azureBlobStorageService).uploadFile(any(), eq("pref"));
    }

    @Test
    void uploadFile_failure_returns500() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "err.pdf", "application/pdf", "x".getBytes());
        when(azureBlobStorageService.uploadFile(any(), any())).thenThrow(new IOException("fail"));

        mockMvc.perform(multipart("/api/files/upload").file(file))
               .andExpect(status().isInternalServerError())
               .andExpect(jsonPath("$.error").exists());

        verify(azureBlobStorageService).uploadFile(any(), any());
    }

    @Test
    void listFiles_returnsList() throws Exception {
        when(azureBlobStorageService.listFiles()).thenReturn(List.of("a.pdf", "b.jpg"));

        mockMvc.perform(get("/api/files/list"))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.length()").value(2));

        verify(azureBlobStorageService).listFiles();
    }

    @Test
    void deleteFile_returnsNoContent() throws Exception {
        doNothing().when(azureBlobStorageService).deleteFile("x.txt");

        mockMvc.perform(delete("/api/files/x.txt"))
               .andExpect(status().isNoContent());

        verify(azureBlobStorageService).deleteFile("x.txt");
    }

    @Test
    void downloadFile_notFound_returns404() throws Exception {
        when(azureBlobStorageService.fileExists("missing.pdf")).thenReturn(false);

        mockMvc.perform(get("/api/files/missing.pdf"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value("Arquivo não encontrado"));

        verify(azureBlobStorageService).fileExists("missing.pdf");
    }

    @Test
    void downloadFile_success_usesBlobContentType_and_returnsBytesAndHeader() throws Exception {
        String storedName = "pref_uuid_original%20nome.png";
        byte[] bytes = "pngbytes".getBytes();

        BlobClient blobClient = mock(BlobClient.class);
        BlobProperties props = mock(BlobProperties.class);

        when(azureBlobStorageService.fileExists(storedName)).thenReturn(true);
        when(azureBlobStorageService.getBlobClient(storedName)).thenReturn(blobClient);
        when(blobClient.getProperties()).thenReturn(props);
        when(props.getContentType()).thenReturn("image/png");
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(bytes));

        mockMvc.perform(get("/api/files/" + storedName))
               .andExpect(status().isOk())
               .andExpect(content().contentType("image/png"))
               .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("original nome.png")))
               .andExpect(content().bytes(bytes));

        verify(azureBlobStorageService).getBlobClient(storedName);
    }

    @Test
    void downloadFile_success_infersContentType_whenBlobHasNoType() throws Exception {
        String storedName = "pfx_uuid_doc.pdf";
        byte[] bytes = "%PDF...".getBytes();

        BlobClient blobClient = mock(BlobClient.class);
        BlobProperties props = mock(BlobProperties.class);

        when(azureBlobStorageService.fileExists(storedName)).thenReturn(true);
        when(azureBlobStorageService.getBlobClient(storedName)).thenReturn(blobClient);
        when(blobClient.getProperties()).thenReturn(props);
        when(props.getContentType()).thenReturn(null); // force infer
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(bytes));

        mockMvc.perform(get("/api/files/" + storedName))
               .andExpect(status().isOk())
               .andExpect(content().contentType("application/pdf"))
               .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("doc.pdf")))
               .andExpect(content().bytes(bytes));
    }
}
