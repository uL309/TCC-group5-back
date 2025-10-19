package puc.airtrack.airtrack;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import puc.airtrack.airtrack.Pecas.Pecas;
import puc.airtrack.airtrack.Pecas.PecasController;
import puc.airtrack.airtrack.Pecas.PecasDTO;
import puc.airtrack.airtrack.Pecas.PecasRepository;
@WebMvcTest(PecasController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PecasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityFilter securityFilter;

    @MockBean
    private TokenService tokenService;
    
    @MockBean
    private PecasRepository pecasRepository;

    // Adicione este MockBean para satisfazer a injeção do controller
    @MockBean
    private puc.airtrack.airtrack.Fornecedor.FornecedorRepo fornecedorRepo;
    
    @Test
    void testCreatePecas() throws Exception {
        PecasDTO dto = new PecasDTO();
        dto.setNome("NomeTeste");
        dto.setNumSerie("SERIE123");
        dto.setCategoria("Mangueiras");
        dto.setDataAquisicao(java.time.LocalDate.now());
        dto.setFornecedorId("1");
        dto.setFornecedorNome("fornecedorTeste");
        dto.setValor((float) 100.0);
        dto.setId_engenheiro(1);
        dto.setStatus(true);

        when(pecasRepository.save(any(Pecas.class))).thenAnswer(i -> {
            Pecas m = i.getArgument(0);
            m.setId(1);
            return m;
        });

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(post("/cpeca")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
    
    @Test
void testGetAllPecas() throws Exception {
    // monta peça com fornecedor
    Pecas peca = new Pecas();
    peca.setId(1);
    peca.setNome("NomeTeste");
    peca.setNum_serie("SERIE123");
    peca.setCategoria("Mangueiras");
    peca.setData_aquisicao(java.time.LocalDate.now());
    peca.setValor(100.0f);
    peca.setId_engenheiro(1);
    peca.setStatus(true);

    puc.airtrack.airtrack.Fornecedor.Fornecedor fornecedor = new puc.airtrack.airtrack.Fornecedor.Fornecedor();
    fornecedor.setId("1");
    fornecedor.setName("fornecedorTeste");
    peca.setFornecedor(fornecedor);

    // mock do repositório de peças (correto)
    when(pecasRepository.findAll()).thenReturn(Arrays.asList(peca));

    mockMvc.perform(get("/gpecas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].nome").value("NomeTeste"))
        .andExpect(jsonPath("$[0].num_serie").value("SERIE123"))
        .andExpect(jsonPath("$[0].fornecedor_nome").value("fornecedorTeste"));
}

    @Test
    void testUpdatePecas() throws Exception {
        PecasDTO dto = new PecasDTO();
        dto.setId(1); // <-- importante: definir id no DTO
        dto.setNome("NomeTeste");
        dto.setNumSerie("SERIE123");
        dto.setCategoria("Mangueiras");
        dto.setDataAquisicao(java.time.LocalDate.now());
        dto.setFornecedorId("1");
        dto.setFornecedorNome("fornecedorTeste");
        dto.setValor((float) 100.0);
        dto.setId_engenheiro(1);
        dto.setStatus(true);

        // entidade existente no banco
        Pecas peca = new Pecas();
        peca.setId(1);
        peca.setNome("NomeTeste");
        peca.setNum_serie("SERIE123");
        peca.setCategoria("Mangueiras");
        peca.setData_aquisicao(java.time.LocalDate.now());
        peca.setValor(100.0f);
        peca.setId_engenheiro(1);
        peca.setStatus(true);

        // fornecedor usado pela peça (controller pode buscar pelo fornecedorId)
        puc.airtrack.airtrack.Fornecedor.Fornecedor fornecedor = new puc.airtrack.airtrack.Fornecedor.Fornecedor();
        fornecedor.setId("1");
        fornecedor.setName("fornecedorTeste");
        peca.setFornecedor(fornecedor);

        // mocks
        when(pecasRepository.findById(1)).thenReturn(Optional.of(peca));
        when(fornecedorRepo.findById("1")).thenReturn(Optional.of(fornecedor));
        when(pecasRepository.save(any(Pecas.class))).thenAnswer(inv -> inv.getArgument(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(put("/upeca")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
@Test
void testBuscarPorId_PecasEncontrado() throws Exception {
    Pecas peca = new Pecas();
    peca.setId(1);
    peca.setNome("NomeTeste");
    peca.setNum_serie("SERIE123");
    peca.setCategoria("Mangueiras");
    peca.setData_aquisicao(java.time.LocalDate.now());
    peca.setValor(100.0f);
    peca.setId_engenheiro(1);
    peca.setStatus(true);

    puc.airtrack.airtrack.Fornecedor.Fornecedor fornecedor = new puc.airtrack.airtrack.Fornecedor.Fornecedor();
    fornecedor.setId("1");
    fornecedor.setName("fornecedorTeste");
    peca.setFornecedor(fornecedor);

    when(pecasRepository.findById(1)).thenReturn(Optional.of(peca));

    mockMvc.perform(get("/gpeca").param("id", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("NomeTeste"))
        .andExpect(jsonPath("$.num_serie").value("SERIE123"))
        .andExpect(jsonPath("$.fornecedor_nome").value("fornecedorTeste"));
}

@Test
void testBuscarPorId_FornecedorNaoEncontrado() throws Exception {
    when(pecasRepository.findById(99)).thenReturn(Optional.empty());

    mockMvc.perform(get("/gpeca").param("id", "99"))
        .andExpect(status().isNotFound());
}
    @Test
    void testDeleteFornecedor() throws Exception {
        Pecas peca = new Pecas();
        peca.setId(1);

        when(pecasRepository.findById(1)).thenReturn(Optional.of(peca));

        mockMvc.perform(get("/dpeca").param("id", "1")) // usar get se controller mapeou GET
            .andExpect(status().isOk());
    }
    
    // Registra o módulo do JavaTime para o ObjectMapper
    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Isso registra o módulo do JavaTime
    }
}

