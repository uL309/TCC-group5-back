package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*; // <-- adicionado
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import puc.airtrack.airtrack.Repositorio;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Motor.Motor;
import puc.airtrack.airtrack.Motor.MotorRepository;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdem;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemController;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemDTO;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemRepository;
import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemService;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemService;
import puc.airtrack.airtrack.OrdemDeServico.OrdemStatus;
import puc.airtrack.airtrack.tipoMotor.TipoMotor;
import puc.airtrack.airtrack.tipoMotor.TipoMotorRepository;

@ExtendWith(MockitoExtension.class)
public class CabecalhoOrdemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;

    @Mock
    private LinhaOrdemService linhaOrdemService;

    @Mock
    private CabecalhoOrdemService cabecalhoOrdemService;

    @Mock
    private puc.airtrack.airtrack.services.AzureBlobStorageService azureBlobStorageService;

    @Mock
    private puc.airtrack.airtrack.services.OrdemServicoPdfService ordemServicoPdfService;
    @Mock
    private MotorRepository motorRepository;
    @Mock
    private TipoMotorRepository tipoMotorRepository;
    @Mock
    private Repositorio userRepository;

    // Segurança/filters não são necessários no standaloneSetup

    @InjectMocks
    private CabecalhoOrdemController cabecalhoOrdemController;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(cabecalhoOrdemController)
                .setMessageConverters(
                    new MappingJackson2HttpMessageConverter(),
                    new ByteArrayHttpMessageConverter()
                )
                .build();
    }

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
        int ordemId = 1;

       // garantir que a ordem exista para que o controller não retorne 404
    CabecalhoOrdem existing = new CabecalhoOrdem();
       existing.setId(ordemId);
      when(cabecalhoOrdemRepository.findById(ordemId)).thenReturn(Optional.of(existing));

        // cenário sucesso: retorna bytes de PDF
        byte[] fakePdf = "%PDF-1.4 fake".getBytes();
        when(ordemServicoPdfService.gerarPdfOrdemServico(ordemId)).thenReturn(fakePdf);

        // usar o endpoint que o controller realmente expõe (ex: "/ordem/{id}/pdf")
        mockMvc.perform(get("/ordem/" + ordemId + "/pdf"))
               .andExpect(status().isOk())
               .andExpect(content().bytes(fakePdf));

        // cenário erro: service lança exceção
        when(ordemServicoPdfService.gerarPdfOrdemServico(ordemId)).thenThrow(new RuntimeException("fail"));

        // aguarde ServletException do MockMvc ou status apropriado conforme seu controller
        mockMvc.perform(get("/ordem/" + ordemId + "/pdf"))
               .andExpect(status().is5xxServerError());
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

    @Test
    void testDownloadAnexo_InferTxtPngJpgJpegCsvJsonXlsxContentTypes() throws Exception {
        CabecalhoOrdem e = new CabecalhoOrdem(); e.setId(30);
        when(cabecalhoOrdemRepository.findById(30)).thenReturn(Optional.of(e));

        String[] files = {
            "ordem_30_a.TXT",
            "ordem_30_b.png",
            "ordem_30_c.JPG",
            "ordem_30_d.jpeg",
            "ordem_30_e.csv",
            "ordem_30_f.json",
            "ordem_30_g.XLSX"
        };
        String[] expected = {
            "text/plain",
            "image/png",
            "image/jpeg",
            "image/jpeg",
            "text/csv",
            "application/json",
            // sheet MIME pode variar; usar conteúdo esperado comum:
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        };

        for (int i = 0; i < files.length; i++) {
            String fileName = files[i];
            when(azureBlobStorageService.fileExists(fileName)).thenReturn(true);

            BlobClient blobClient = mock(BlobClient.class);
            BlobProperties props = mock(BlobProperties.class);
            // Para .csv e .json use o contentType do blob; demais forçam inferência
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".csv")) {
                when(props.getContentType()).thenReturn("text/csv");
            } else if (lower.endsWith(".json")) {
                when(props.getContentType()).thenReturn("application/json");
            } else {
                when(props.getContentType()).thenReturn(null);
            }
            when(blobClient.getProperties()).thenReturn(props);
            when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(("data"+i).getBytes(StandardCharsets.UTF_8)));
            when(azureBlobStorageService.getBlobClient(fileName)).thenReturn(blobClient);

            mockMvc.perform(get("/ordem/30/anexos/" + fileName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(expected[i]));
        }
    }

    @Test
    void testDownloadAnexo_InferPdfUppercaseAndMultiDot() throws Exception {
        CabecalhoOrdem e = new CabecalhoOrdem(); e.setId(31);
        when(cabecalhoOrdemRepository.findById(31)).thenReturn(Optional.of(e));
        String fileName = "ordem_31.version.2.PDF";
        when(azureBlobStorageService.fileExists(fileName)).thenReturn(true);

        BlobClient blobClient = mock(BlobClient.class);
        BlobProperties props = mock(BlobProperties.class);
        when(props.getContentType()).thenReturn(null);
        when(blobClient.getProperties()).thenReturn(props);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes("pdfmulti".getBytes(StandardCharsets.UTF_8)));
        when(azureBlobStorageService.getBlobClient(fileName)).thenReturn(blobClient);

        mockMvc.perform(get("/ordem/31/anexos/" + fileName))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void inferContentType_reflection_casosFallback() throws Exception {
        var m = CabecalhoOrdemController.class.getDeclaredMethod("inferContentType", String.class);
        m.setAccessible(true);

        // null provoca NPE no método atual; valide isso explicitamente
        assertThrows(NullPointerException.class, () -> {
            try {
                m.invoke(cabecalhoOrdemController, new Object[]{null});
            } catch (java.lang.reflect.InvocationTargetException ite) {
                Throwable cause = ite.getTargetException();
                if (cause instanceof NullPointerException npe) throw npe;
                throw new RuntimeException(cause);
            }
        });
        assertEquals("application/octet-stream", m.invoke(cabecalhoOrdemController, ""));
        assertEquals("application/octet-stream", m.invoke(cabecalhoOrdemController, "semextensao"));
        assertEquals("application/octet-stream", m.invoke(cabecalhoOrdemController, "final."));
        assertEquals("application/octet-stream", m.invoke(cabecalhoOrdemController, ".env"));
        assertEquals("application/octet-stream", m.invoke(cabecalhoOrdemController, "arquivo.desconhecidoext"));
    }

    @Test
    void adminStats_cenarioCompleto_cobreBranches() throws Exception {
        java.time.LocalDate now = java.time.LocalDate.now();

        // OS concluídas (para taxaConclusao)
        CabecalhoOrdem osConc1 = new CabecalhoOrdem();
        osConc1.setId(100);
        osConc1.setStatus(OrdemStatus.CONCLUIDA);
        osConc1.setTempoUsado(2f);

        CabecalhoOrdem osConc2 = new CabecalhoOrdem();
        osConc2.setId(101);
        osConc2.setStatus(OrdemStatus.CONCLUIDA);
        osConc2.setTempoUsado(3f);

        // OS andamento
        CabecalhoOrdem osAnd = new CabecalhoOrdem();
        osAnd.setId(102);
        osAnd.setStatus(OrdemStatus.ANDAMENTO);
        osAnd.setTempoUsado(1f);

        // OS pendentes: >7 dias, ==7 dias, <7 dias, null, empty, inválida
        CabecalhoOrdem pendMaior7 = new CabecalhoOrdem();
        pendMaior7.setId(200);
        pendMaior7.setStatus(OrdemStatus.PENDENTE);
        pendMaior7.setDataAbertura(now.minusDays(8).toString());

        CabecalhoOrdem pendIgual7 = new CabecalhoOrdem();
        pendIgual7.setId(201);
        pendIgual7.setStatus(OrdemStatus.PENDENTE);
        pendIgual7.setDataAbertura(now.minusDays(7).toString());

        CabecalhoOrdem pendMenor7 = new CabecalhoOrdem();
        pendMenor7.setId(202);
        pendMenor7.setStatus(OrdemStatus.PENDENTE);
        pendMenor7.setDataAbertura(now.minusDays(3).toString());

        CabecalhoOrdem pendNull = new CabecalhoOrdem();
        pendNull.setId(203);
        pendNull.setStatus(OrdemStatus.PENDENTE);
        pendNull.setDataAbertura(null);

        CabecalhoOrdem pendEmpty = new CabecalhoOrdem();
        pendEmpty.setId(204);
        pendEmpty.setStatus(OrdemStatus.PENDENTE);
        pendEmpty.setDataAbertura("");

        CabecalhoOrdem pendInvalida = new CabecalhoOrdem();
        pendInvalida.setId(205);
        pendInvalida.setStatus(OrdemStatus.PENDENTE);
        pendInvalida.setDataAbertura("2025-13-40"); // força catch

        // Listas simuladas
        var todas = java.util.List.of(osConc1, osConc2, osAnd,
                pendMaior7, pendIgual7, pendMenor7, pendNull, pendEmpty, pendInvalida);
        var concluidas = java.util.List.of(osConc1, osConc2);
        var andamento = java.util.List.of(osAnd);
        var pendentes = java.util.List.of(pendMaior7, pendIgual7, pendMenor7, pendNull, pendEmpty, pendInvalida);

        when(cabecalhoOrdemRepository.findAllByOrderByIdDesc()).thenReturn(todas);
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.CONCLUIDA)).thenReturn(concluidas);
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.ANDAMENTO)).thenReturn(andamento);
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.PENDENTE)).thenReturn(pendentes);

        // Motores: ativo expirado, ativo próximo (<100), ativo com tipo null, ativo TBO=0, inativo, status null
        Motor mExp = new Motor(); mExp.setId(1); mExp.setStatus(true); mExp.setMarca("A"); mExp.setModelo("X"); mExp.setHoras_operacao(120);
        Motor mNear = new Motor(); mNear.setId(2); mNear.setStatus(true); mNear.setMarca("A"); mNear.setModelo("X"); mNear.setHoras_operacao(90);
        Motor mTipoNull = new Motor(); mTipoNull.setId(3); mTipoNull.setStatus(true); mTipoNull.setMarca("B"); mTipoNull.setModelo("Y"); mTipoNull.setHoras_operacao(500);
        Motor mTboZero = new Motor(); mTboZero.setId(4); mTboZero.setStatus(true); mTboZero.setMarca("C"); mTboZero.setModelo("Z"); mTboZero.setHoras_operacao(10);
        Motor mInativo = new Motor(); mInativo.setId(5); mInativo.setStatus(false); mInativo.setMarca("D"); mInativo.setModelo("W"); mInativo.setHoras_operacao(1000);
        Motor mStatusNull = new Motor(); mStatusNull.setId(6); mStatusNull.setStatus(null); mStatusNull.setMarca("E"); mStatusNull.setModelo("Q"); mStatusNull.setHoras_operacao(1000);

        when(motorRepository.findAll()).thenReturn(java.util.List.of(mExp, mNear, mTipoNull, mTboZero, mInativo, mStatusNull));

        TipoMotor tipo100 = new TipoMotor(); tipo100.setTbo(100);
        TipoMotor tipoZero = new TipoMotor(); tipoZero.setTbo(0);

        when(tipoMotorRepository.findByMarcaAndModelo("A","X")).thenReturn(tipo100);
        when(tipoMotorRepository.findByMarcaAndModelo("B","Y")).thenReturn(null);
        when(tipoMotorRepository.findByMarcaAndModelo("C","Z")).thenReturn(tipoZero);
        // when(tipoMotorRepository.findByMarcaAndModelo("D","W")).thenReturn(tipo100); // inativo -> não usado
        // when(tipoMotorRepository.findByMarcaAndModelo("E","Q")).thenReturn(tipo100); // status null -> não usado

        // Usuários ativos/inativos/status null
        User uAtivo = new User(); uAtivo.setStatus(true);
        User uInativo = new User(); uInativo.setStatus(false);
        User uNull = new User(); uNull.setStatus(null);
        when(userRepository.findAll()).thenReturn(java.util.List.of(uAtivo, uInativo, uNull));

        var res = mockMvc.perform(get("/ordem/admin/stats"))
                .andExpect(status().isOk()).andReturn();

        var json = res.getResponse().getContentAsString();
        // Parse simples
        com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);

        assertEquals(todas.size(), root.get("total_os").asInt());
        assertEquals(concluidas.size(), root.get("os_concluidas").asInt());
        assertEquals(andamento.size(), root.get("os_em_andamento").asInt());
        assertEquals(pendentes.size(), root.get("os_pendentes").asInt());

        // Criticas: >7 e ==7 dias => 2
        assertEquals(2, root.get("os_pendentes_criticas").asInt());

        // Motores expirados: apenas mExp (>=100%) => 1
        assertEquals(1, root.get("motores_tbo_expirado").asInt());

        // Usuarios ativos: apenas uAtivo => 1
        assertEquals(1, root.get("total_usuarios_ativos").asInt());

        // Taxa conclusão >0 (2 / 9 *100)
        assertTrue(root.get("taxa_conclusao_geral").asDouble() > 0);
    }

    @Test
    void adminStats_semDados_taxaZero_eContadoresZero() throws Exception {
        when(cabecalhoOrdemRepository.findAllByOrderByIdDesc()).thenReturn(java.util.List.of());
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.CONCLUIDA)).thenReturn(java.util.List.of());
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.ANDAMENTO)).thenReturn(java.util.List.of());
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.PENDENTE)).thenReturn(java.util.List.of());
        when(motorRepository.findAll()).thenReturn(java.util.List.of());
        when(userRepository.findAll()).thenReturn(java.util.List.of());

        var res = mockMvc.perform(get("/ordem/admin/stats"))
                .andExpect(status().isOk()).andReturn();
        com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(res.getResponse().getContentAsString());

        assertEquals(0, root.get("total_os").asInt());
        assertEquals(0, root.get("os_concluidas").asInt());
        assertEquals(0, root.get("os_em_andamento").asInt());
        assertEquals(0, root.get("os_pendentes").asInt());
        assertEquals(0, root.get("os_pendentes_criticas").asInt());
        assertEquals(0, root.get("motores_tbo_expirado").asInt());
        assertEquals(0, root.get("total_usuarios_ativos").asInt());
        assertEquals(0.0, root.get("taxa_conclusao_geral").asDouble(), 0.0001);
    }

    @Test
    void getRiscos_semDados_tudoZero_taxaBaixaIncrementaRiscoExtra() throws Exception {
        when(motorRepository.findAll()).thenReturn(java.util.List.of());
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.PENDENTE)).thenReturn(java.util.List.of());
        when(cabecalhoOrdemRepository.findAllByOrderByIdDesc()).thenReturn(java.util.List.of());
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.CONCLUIDA)).thenReturn(java.util.List.of());

        var res = mockMvc.perform(get("/ordem/auditor/riscos"))
                .andExpect(status().isOk()).andReturn();
        var root = mapper.readTree(res.getResponse().getContentAsString());

        assertEquals(0, root.get("motores_tbo_expirado").asInt());
        assertEquals(0, root.get("os_pendentes_criticas").asInt());
        assertEquals(0, root.get("taxa_conclusao").asInt());
        assertTrue(root.get("taxa_conclusao_baixa").asBoolean());
        // total_riscos = 0 + 0 + 1 (taxa baixa)
        assertEquals(1, root.get("total_riscos").asInt());
    }

    @Test
    void getRiscos_taxaConclusaoAlta_naoIncrementaRiscoExtra() throws Exception {
        // 2 OS totais, 1 concluída => 50% (não baixa)
        CabecalhoOrdem concluida = new CabecalhoOrdem(); concluida.setStatus(OrdemStatus.CONCLUIDA);
        CabecalhoOrdem pendente = new CabecalhoOrdem(); pendente.setStatus(OrdemStatus.PENDENTE); pendente.setDataAbertura(LocalDate.now().toString());
        when(cabecalhoOrdemRepository.findAllByOrderByIdDesc()).thenReturn(java.util.List.of(concluida, pendente));
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.CONCLUIDA)).thenReturn(java.util.List.of(concluida));
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.PENDENTE)).thenReturn(java.util.List.of(pendente));
        when(motorRepository.findAll()).thenReturn(java.util.List.of());
        var res = mockMvc.perform(get("/ordem/auditor/riscos"))
                .andExpect(status().isOk()).andReturn();
        var root = mapper.readTree(res.getResponse().getContentAsString());

        assertEquals(50, root.get("taxa_conclusao").asInt());
        assertFalse(root.get("taxa_conclusao_baixa").asBoolean());
        // Sem motores expirados e sem pendentes críticas (data hoje), total_riscos = 0
        assertEquals(0, root.get("total_riscos").asInt());
    }

    /*@Test
    void getRiscos_motores_variacoesTbo_e_pendentes_datasDiversas() throws Exception {
        LocalDate now = LocalDate.now();
        Motor statusNull = new Motor(); statusNull.setMarca("A"); statusNull.setModelo("X");
        Motor inativo = new Motor(); inativo.setStatus(false); inativo.setMarca("B"); inativo.setModelo("Y");
        Motor tipoNull = new Motor(); tipoNull.setStatus(true); tipoNull.setMarca("C"); tipoNull.setModelo("Z"); tipoNull.setHoras_operacao(10);
        Motor tboZero = new Motor(); tboZero.setStatus(true); tboZero.setMarca("D"); tboZero.setModelo("W"); tboZero.setHoras_operacao(500);
        Motor abaixo = new Motor(); abaixo.setStatus(true); abaixo.setMarca("E"); abaixo.setModelo("Q"); abaixo.setHoras_operacao(40);
        Motor expirado = new Motor(); expirado.setStatus(true); expirado.setMarca("F"); expirado.setModelo("R"); expirado.setHoras_operacao(150);
        when(motorRepository.findAll()).thenReturn(java.util.List.of(statusNull, inativo, tipoNull, tboZero, abaixo, expirado));

        TipoMotor tipo100 = new TipoMotor(); tipo100.setTbo(100);
        TipoMotor tipo50 = new TipoMotor(); tipo50.setTbo(50);
        TipoMotor tipoZeroTm = new TipoMotor(); tipoZeroTm.setTbo(0);
        when(tipoMotorRepository.findByMarcaAndModelo("A","X")).thenReturn(tipo100);
        when(tipoMotorRepository.findByMarcaAndModelo("B","Y")).thenReturn(null);
        when(tipoMotorRepository.findByMarcaAndModelo("C","Z")).thenReturn(tipoZeroTm);
        when(tipoMotorRepository.findByMarcaAndModelo("D","W")).thenReturn(tipoZeroTm);
        when(tipoMotorRepository.findByMarcaAndModelo("E","Q")).thenReturn(tipo100);
        when(tipoMotorRepository.findByMarcaAndModelo("F","R")).thenReturn(tipo50);

        // Pendentes variadas
        CabecalhoOrdem pendCritico = new CabecalhoOrdem(); pendCritico.setStatus(OrdemStatus.PENDENTE);
        pendCritico.setDataAbertura(now.minusDays(8).toString()); // conta
        CabecalhoOrdem pendIgual7 = new CabecalhoOrdem(); pendIgual7.setStatus(OrdemStatus.PENDENTE);
        pendIgual7.setDataAbertura(now.minusDays(7).toString()); // conta (==7)
        CabecalhoOrdem pendRecente = new CabecalhoOrdem(); pendRecente.setStatus(OrdemStatus.PENDENTE);
        pendRecente.setDataAbertura(now.minusDays(2).toString()); // não
        CabecalhoOrdem pendDataInvalida = new CabecalhoOrdem(); pendDataInvalida.setStatus(OrdemStatus.PENDENTE);
        pendDataInvalida.setDataAbertura("2025-13-40"); // catch
        CabecalhoOrdem pendDataNull = new CabecalhoOrdem(); pendDataNull.setStatus(OrdemStatus.PENDENTE);
        pendDataNull.setDataAbertura(null); // ignora
        CabecalhoOrdem pendDataEmpty = new CabecalhoOrdem(); pendDataEmpty.setStatus(OrdemStatus.PENDENTE);
        pendDataEmpty.setDataAbertura(""); // ignora
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.PENDENTE))
                .thenReturn(java.util.List.of(pendCritico, pendIgual7, pendRecente, pendDataInvalida, pendDataNull, pendDataEmpty));

        // Concluídas / todas para taxaConclusao >0 mas <50 (baixa)
        CabecalhoOrdem concl = new CabecalhoOrdem(); concl.setStatus(OrdemStatus.CONCLUIDA);
        CabecalhoOrdem pend = new CabecalhoOrdem(); pend.setStatus(OrdemStatus.PENDENTE);
        when(cabecalhoOrdemRepository.findAllByOrderByIdDesc()).thenReturn(java.util.List.of(concl, pend));
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.CONCLUIDA)).thenReturn(java.util.List.of(concl));

        var res = mockMvc.perform(get("/ordem/auditor/riscos"))
                .andExpect(status().isOk()).andReturn();
        var root = mapper.readTree(res.getResponse().getContentAsString());

        // Somente motor expirado conta => 1
        assertEquals(1, root.get("motores_tbo_expirado").asInt());
        // Pendentes críticas: 8 dias e 7 dias => 2
        assertEquals(2, root.get("os_pendentes_criticas").asInt());
        // Taxa conclusão 1/2 = 50 -> não baixa
        assertEquals(50, root.get("taxa_conclusao").asInt());
        assertFalse(root.get("taxa_conclusao_baixa").asBoolean());
        // total_riscos = 1 + 2 (sem incremento extra) = 3
        assertEquals(3, root.get("total_riscos").asInt());
    }*/

    @Test
    void getRiscos_taxaMuitoBaixa_incrementaTotalRiscos() throws Exception {
        // 1 concluída de 3 => 33%
        CabecalhoOrdem c = new CabecalhoOrdem(); c.setStatus(OrdemStatus.CONCLUIDA);
        CabecalhoOrdem p1 = new CabecalhoOrdem(); p1.setStatus(OrdemStatus.PENDENTE); p1.setDataAbertura(LocalDate.now().minusDays(8).toString());
        CabecalhoOrdem p2 = new CabecalhoOrdem(); p2.setStatus(OrdemStatus.PENDENTE); p2.setDataAbertura(LocalDate.now().toString());

        when(cabecalhoOrdemRepository.findAllByOrderByIdDesc()).thenReturn(java.util.List.of(c, p1, p2));
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.CONCLUIDA)).thenReturn(java.util.List.of(c));
        when(cabecalhoOrdemRepository.findByStatusOrderByIdDesc(OrdemStatus.PENDENTE)).thenReturn(java.util.List.of(p1, p2));

        // Moto expirado para somar risco
        Motor m = new Motor(); m.setStatus(true); m.setMarca("X"); m.setModelo("M"); m.setHoras_operacao(200);
        when(motorRepository.findAll()).thenReturn(java.util.List.of(m));
        TipoMotor tipo = new TipoMotor(); tipo.setTbo(100);
        when(tipoMotorRepository.findByMarcaAndModelo("X","M")).thenReturn(tipo);

        var res = mockMvc.perform(get("/ordem/auditor/riscos"))
                .andExpect(status().isOk()).andReturn();
        var root = mapper.readTree(res.getResponse().getContentAsString());

        assertEquals(33, root.get("taxa_conclusao").asInt());
        assertTrue(root.get("taxa_conclusao_baixa").asBoolean());
        // riscos: motores_tbo_expirado=1 + os_pendentes_criticas=1 + taxa baixa extra=1 => 3
        assertEquals(3, root.get("total_riscos").asInt());
    }
}
