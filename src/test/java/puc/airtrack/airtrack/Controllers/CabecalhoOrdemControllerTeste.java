package puc.airtrack.airtrack.Controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdem;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemController;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemDTO;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemRepository;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemService;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemService;

@WebMvcTest(CabecalhoOrdemController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CabecalhoOrdemControllerTeste {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;

    @MockBean
    private LinhaOrdemService linhaOrdemService;

    @MockBean
    private CabecalhoOrdemService cabecalhoOrdemService;

    @MockBean
    private puc.airtrack.airtrack.services.AzureBlobStorageService azureBlobStorageService;

    @MockBean
    private puc.airtrack.airtrack.services.OrdemServicoPdfService ordemServicoPdfService;

    @MockBean
    private puc.airtrack.airtrack.TokenService tokenService;

    @MockBean
    private puc.airtrack.airtrack.SecurityFilter securityFilter;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testGetCabecalho_Found() throws Exception {
        CabecalhoOrdem entity = new CabecalhoOrdem();
        entity.setId(1);
        entity.setDataAbertura("01/01/2025");
        entity.setDescricao("desc");
        entity.setDataFechamento("02/01/2025");
        puc.airtrack.airtrack.Cliente.Cliente cliente = new puc.airtrack.airtrack.Cliente.Cliente();
        cliente.setCpf("12345678900");
        cliente.setName("Cliente X");
        entity.setCliente(cliente);
        puc.airtrack.airtrack.Motor.Motor motor = new puc.airtrack.airtrack.Motor.Motor();
        motor.setId(10);
        motor.setSerie_motor("SER123");
        entity.setNumSerieMotor(motor);
        puc.airtrack.airtrack.Login.User sup = new puc.airtrack.airtrack.Login.User();
        sup.setId(2); sup.setName("SupervisorY");
        entity.setSupervisor(sup);
        puc.airtrack.airtrack.Login.User eng = new puc.airtrack.airtrack.Login.User();
        eng.setId(3); eng.setName("EngenheiroX");
        entity.setEngenheiroAtuante(eng);
        entity.setTempoUsado(2.5f);
        entity.setTempoEstimado(5);
        entity.setValorHora(120.0f);
        entity.setStatus(puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.ANDAMENTO);

        when(cabecalhoOrdemRepository.findById(1)).thenReturn(Optional.of(entity));
        when(linhaOrdemService.findByCabecalhoId(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/ordem/get").param("id", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.descricao").value("desc"))
            .andExpect(jsonPath("$.cliente").value("12345678900"))
            .andExpect(jsonPath("$.motor_nome").value("SER123"))
            .andExpect(jsonPath("$.supervisor_nome").value("SupervisorY"))
            .andExpect(jsonPath("$.engenheiro_atuante_nome").value("EngenheiroX"))
            .andExpect(jsonPath("$.status").value(puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.ANDAMENTO.getStatus()));
    }

    @Test
    void testGetCabecalho_NotFound() throws Exception {
        when(cabecalhoOrdemRepository.findById(999)).thenReturn(Optional.empty());
        mockMvc.perform(get("/ordem/get").param("id", "999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllCabecalhos_EmptyAndNonEmpty() throws Exception {
        CabecalhoOrdem e1 = new CabecalhoOrdem();
        e1.setId(1); e1.setDescricao("A");
        e1.setStatus(puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.ANDAMENTO);
        e1.setValorHora(0.0f);
        CabecalhoOrdem e2 = new CabecalhoOrdem();
        e2.setId(2); e2.setDescricao("B");
        e2.setStatus(puc.airtrack.airtrack.OrdemDeServico.OrdemStatus.PENDENTE);
        e2.setValorHora(0.0f);

        when(cabecalhoOrdemRepository.findAllByOrderByIdDesc()).thenReturn(Arrays.asList(e2, e1));
        mockMvc.perform(get("/ordem/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[1].id").value(1));

        // empty list case
        when(cabecalhoOrdemRepository.findAllByOrderByIdDesc()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/ordem/list"))
            .andExpect(status().isOk())
            .andExpect(content().string("[]"));
    }

    @Test
    void testCreateAndUpdate_DelegateToService() throws Exception {
        CabecalhoOrdemDTO dto = new CabecalhoOrdemDTO();
        dto.setDescricao("nova");
        dto.setTempoUsado(0.0f); dto.setValorHora(0.0f);

        when(cabecalhoOrdemService.createCabecalho(any(CabecalhoOrdemDTO.class)))
            .thenReturn(org.springframework.http.ResponseEntity.created(null).body("ok"));
        mockMvc.perform(post("/ordem/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
            .andExpect(status().isCreated());

        when(cabecalhoOrdemService.updateCabecalho(any(CabecalhoOrdemDTO.class)))
            .thenReturn(org.springframework.http.ResponseEntity.ok("updated"));
        mockMvc.perform(put("/ordem/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test
    void testDeleteCabecalho_FoundAndNotFound() throws Exception {
        CabecalhoOrdem e = new CabecalhoOrdem(); e.setId(5);
        when(cabecalhoOrdemRepository.findById(5)).thenReturn(Optional.of(e));
        mockMvc.perform(delete("/ordem/delete").param("id", "5"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("deleted")));
        verify(cabecalhoOrdemRepository).deleteById(5);

        when(cabecalhoOrdemRepository.findById(42)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/ordem/delete").param("id", "42"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testAtualizarStatus_DelegateToService() throws Exception {
        when(cabecalhoOrdemService.atualizarStatusCabecalho(1, 2))
            .thenReturn(org.springframework.http.ResponseEntity.ok("ok"));
        mockMvc.perform(put("/ordem/atualizar-status")
                .param("cabecalhoId", "1")
                .param("status", "2"))
            .andExpect(status().isOk());
    }

    @Test
    void testUploadAnexo_SuccessAndNotFound() throws Exception {
        CabecalhoOrdem e = new CabecalhoOrdem(); e.setId(7);
        when(cabecalhoOrdemRepository.findById(7)).thenReturn(Optional.of(e));
        when(azureBlobStorageService.uploadFile(any(), anyString())).thenReturn("https://blob/x");
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", MediaType.TEXT_PLAIN_VALUE, "conteudo".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/ordem/7/anexos").file(file).with(request -> { request.setMethod("POST"); return request; }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileUrl").value("https://blob/x"));

        when(cabecalhoOrdemRepository.findById(100)).thenReturn(Optional.empty());
        mockMvc.perform(multipart("/ordem/100/anexos").file(file).with(request -> { request.setMethod("POST"); return request; }))
            .andExpect(status().isNotFound());
    }

    @Test
    void testListAnexos_SuccessAndNotFound() throws Exception {
        CabecalhoOrdem e = new CabecalhoOrdem(); e.setId(9);
        when(cabecalhoOrdemRepository.findById(9)).thenReturn(Optional.of(e));
        when(azureBlobStorageService.listFilesWithPrefix("ordem_9")).thenReturn(Arrays.asList("ordem_9_file.pdf"));
        when(azureBlobStorageService.getFileUrl("ordem_9_file.pdf")).thenReturn("https://blob/file.pdf");

        mockMvc.perform(get("/ordem/9/anexos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("ordem_9_file.pdf"));

        when(cabecalhoOrdemRepository.findById(8)).thenReturn(Optional.empty());
        mockMvc.perform(get("/ordem/8/anexos"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testDownloadAnexo_Success_NotFound_FileMissing() throws Exception {
        CabecalhoOrdem e = new CabecalhoOrdem(); e.setId(11);
        String fileName = "ordem_11_uuid_file.txt";
        when(cabecalhoOrdemRepository.findById(11)).thenReturn(Optional.of(e));
        when(azureBlobStorageService.fileExists(fileName)).thenReturn(true);

        // mock BlobClient behavior
        BlobClient blobClient = mock(BlobClient.class);
        BlobProperties props = mock(BlobProperties.class);
        when(props.getContentType()).thenReturn("text/plain");
        when(blobClient.getProperties()).thenReturn(props);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes("hello".getBytes(StandardCharsets.UTF_8)));
        when(azureBlobStorageService.getBlobClient(fileName)).thenReturn(blobClient);

        mockMvc.perform(get("/ordem/11/anexos/" + fileName))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("filename=\"")))
            .andExpect(content().bytes("hello".getBytes(StandardCharsets.UTF_8)));

        // file not found at storage
        when(azureBlobStorageService.fileExists("missing.pdf")).thenReturn(false);
        mockMvc.perform(get("/ordem/11/anexos/missing.pdf"))
            .andExpect(status().isNotFound());

        // ordem not found
        when(cabecalhoOrdemRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/ordem/99/anexos/some.pdf"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testExcluirAnexo_Success_NotFound_FileMissing() throws Exception {
        CabecalhoOrdem e = new CabecalhoOrdem(); e.setId(12);
        when(cabecalhoOrdemRepository.findById(12)).thenReturn(Optional.of(e));
        when(azureBlobStorageService.fileExists("ordem_12_f.pdf")).thenReturn(true);

        mockMvc.perform(delete("/ordem/12/anexos/ordem_12_f.pdf"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Arquivo excluído com sucesso"));

        // file missing
        when(azureBlobStorageService.fileExists("no.pdf")).thenReturn(false);
        mockMvc.perform(delete("/ordem/12/anexos/no.pdf"))
            .andExpect(status().isNotFound());

        // ordem not found
        when(cabecalhoOrdemRepository.findById(999)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/ordem/999/anexos/x.pdf"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGerarPdf_Success_And_Error() throws Exception {
        CabecalhoOrdem e = new CabecalhoOrdem(); e.setId(3);
        when(cabecalhoOrdemRepository.findById(3)).thenReturn(Optional.of(e));
        byte[] pdf = "pdfbytes".getBytes(StandardCharsets.UTF_8);
        when(ordemServicoPdfService.gerarPdfOrdemServico(3)).thenReturn(pdf);

        mockMvc.perform(get("/ordem/3/pdf"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("ordem_servico_3.pdf")))
            .andExpect(content().contentType("application/pdf"))
            .andExpect(content().bytes(pdf));

        // error during PDF generation
        when(cabecalhoOrdemRepository.findById(4)).thenReturn(Optional.of(new CabecalhoOrdem()));
        when(ordemServicoPdfService.gerarPdfOrdemServico(4)).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/ordem/4/pdf"))
            .andExpect(status().isInternalServerError());
    }
    @Test
    void testDownloadAnexo_InferPdfContentTypeWhenBlobHasNoContentType() throws Exception {
        CabecalhoOrdem e = new CabecalhoOrdem(); e.setId(20);
        String fileName = "ordem_20_uuid_file.pdf";

        when(cabecalhoOrdemRepository.findById(20)).thenReturn(Optional.of(e));
        when(azureBlobStorageService.fileExists(fileName)).thenReturn(true);

        BlobClient blobClient = mock(BlobClient.class);
        BlobProperties props = mock(BlobProperties.class);
        when(props.getContentType()).thenReturn(null); // força uso de inferContentType
        when(blobClient.getProperties()).thenReturn(props);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes("pdfdata".getBytes(StandardCharsets.UTF_8)));
        when(azureBlobStorageService.getBlobClient(fileName)).thenReturn(blobClient);

        mockMvc.perform(get("/ordem/20/anexos/" + fileName))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/pdf"))
            .andExpect(content().bytes("pdfdata".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testDownloadAnexo_InferDocxContentTypeWhenBlobHasNoContentType() throws Exception {
        CabecalhoOrdem e = new CabecalhoOrdem(); e.setId(21);
        String fileName = "ordem_21_uuid_file.docx";

        when(cabecalhoOrdemRepository.findById(21)).thenReturn(Optional.of(e));
        when(azureBlobStorageService.fileExists(fileName)).thenReturn(true);

        BlobClient blobClient = mock(BlobClient.class);
        BlobProperties props = mock(BlobProperties.class);
        when(props.getContentType()).thenReturn(null);
        when(blobClient.getProperties()).thenReturn(props);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes("docxdata".getBytes(StandardCharsets.UTF_8)));
        when(azureBlobStorageService.getBlobClient(fileName)).thenReturn(blobClient);

        mockMvc.perform(get("/ordem/21/anexos/" + fileName))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            .andExpect(content().bytes("docxdata".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testDownloadAnexo_InferDefaultContentTypeWhenUnknownExtension() throws Exception {
        CabecalhoOrdem e = new CabecalhoOrdem(); e.setId(22);
        String fileName = "ordem_22_uuid_file.unknownext";

        when(cabecalhoOrdemRepository.findById(22)).thenReturn(Optional.of(e));
        when(azureBlobStorageService.fileExists(fileName)).thenReturn(true);

        BlobClient blobClient = mock(BlobClient.class);
        BlobProperties props = mock(BlobProperties.class);
        when(props.getContentType()).thenReturn(null);
        when(blobClient.getProperties()).thenReturn(props);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes("raw".getBytes(StandardCharsets.UTF_8)));
        when(azureBlobStorageService.getBlobClient(fileName)).thenReturn(blobClient);

        mockMvc.perform(get("/ordem/22/anexos/" + fileName))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/octet-stream"))
            .andExpect(content().bytes("raw".getBytes(StandardCharsets.UTF_8)));
    }
}