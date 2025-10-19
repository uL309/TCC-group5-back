package puc.airtrack.airtrack;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import puc.airtrack.airtrack.Fornecedor.Fornecedor;
import puc.airtrack.airtrack.Fornecedor.FornecedorController;
import puc.airtrack.airtrack.Fornecedor.FornecedorDTO;
import puc.airtrack.airtrack.Fornecedor.FornecedorRepo;

@WebMvcTest(FornecedorController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FornecedorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityFilter securityFilter;

    @MockBean
    private TokenService tokenService;
    
    @MockBean
    private FornecedorRepo fornecedorRepository;

    @Test
    void testCreateFornecedor() throws Exception {
        FornecedorDTO dto = new FornecedorDTO();
        dto.setName("NomeTeste");
        dto.setEmail("emailteste@mail.com");
        dto.setCategoria("Mangueiras");
        dto.setContato("41999999999");
        dto.setStatus(true);

        when(fornecedorRepository.save(any(Fornecedor.class))).thenAnswer(i -> {
            Fornecedor m = i.getArgument(0);
            m.setId("1");
            return m;
        });

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(post("/cforn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
    
    @Test
    void testGetAllFornecedores() throws Exception {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId("1");
        fornecedor.setName("NomeTeste");
        fornecedor.setEmail("emailteste@mail.com");
        fornecedor.setCategoria("Mangueiras");
        fornecedor.setContato("41999999999");
        fornecedor.setStatus(true);
        List<Fornecedor> fornecedores = Arrays.asList(fornecedor);
        when(fornecedorRepository.findAll()).thenReturn(fornecedores);

        mockMvc.perform(get("/gforns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("NomeTeste"));
    }

    @Test
    void testUpdateFornecedor() throws Exception {
        FornecedorDTO dto = new FornecedorDTO();
        dto.setId("1");
        dto.setName("NomeTeste");
        dto.setEmail("emailteste@mail.com");
        dto.setCategoria("Mangueiras");
        dto.setContato("41999999999");
        dto.setStatus(true);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId("1");

        when(fornecedorRepository.findById("1")).thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedor);

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(put("/uforn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
@Test
void testBuscarPorId_FornecedorEncontrado() throws Exception {
    Fornecedor fornecedor = new Fornecedor();
    fornecedor.setId("1");
        fornecedor.setName("NomeTeste");
        fornecedor.setEmail("emailteste@mail.com");
        fornecedor.setCategoria("Mangueiras");
        fornecedor.setContato("41999999999");
        fornecedor.setStatus(true);

    when(fornecedorRepository.findById("1")).thenReturn(Optional.of(fornecedor));

    mockMvc.perform(get("/gforn").param("id", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cnpj").value("1"))
        .andExpect(jsonPath("$.name").value("NomeTeste"));
}

@Test
void testBuscarPorId_FornecedorNaoEncontrado() throws Exception {
    when(fornecedorRepository.findById("99")).thenReturn(Optional.empty());

    mockMvc.perform(get("/gmotor").param("param", "99"))
        .andExpect(status().isNotFound());
}
    @Test
    void testDeleteFornecedor() throws Exception {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId("1");

        when(fornecedorRepository.findById("1")).thenReturn(Optional.of(fornecedor));

        mockMvc.perform(delete("/dforn").param("id", "1"))
    .andExpect(status().isOk());
    }
    
    @Test
    void testCreateFornecedorAlreadyExists() throws Exception {
        FornecedorDTO dto = new FornecedorDTO();
        dto.setId("1");
        dto.setName("NomeExistente");

        Fornecedor existing = new Fornecedor();
        existing.setId("1");

        when(fornecedorRepository.findById("1")).thenReturn(Optional.of(existing));

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(post("/cforn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateFornecedor_CheckLocationHeader() throws Exception {
        FornecedorDTO dto = new FornecedorDTO();
        dto.setId("1");
        dto.setName("NovoNome");
        dto.setEmail("e@mail");
        dto.setCategoria("Cat");
        dto.setContato("contato");
        dto.setStatus(true);

        when(fornecedorRepository.findById("1")).thenReturn(Optional.empty());
        when(fornecedorRepository.save(any(Fornecedor.class))).thenAnswer(i -> {
            Fornecedor f = i.getArgument(0);
            f.setId("1");
            return f;
        });

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(post("/cforn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/gforn?param=1"));
    }

    @Test
    void testUpdateFornecedor_NotFound() throws Exception {
        FornecedorDTO dto = new FornecedorDTO();
        dto.setId("99");
        dto.setName("Novo");

        when(fornecedorRepository.findById("99")).thenReturn(Optional.empty());

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(put("/uforn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateFornecedor_ChangesFields() throws Exception {
        FornecedorDTO dto = new FornecedorDTO();
        dto.setId("1");
        dto.setName("NomeNovo");
        dto.setEmail("novo@mail");
        dto.setCategoria("NovaCat");
        dto.setContato("99999");
        dto.setStatus(false);

        Fornecedor existing = new Fornecedor();
        existing.setId("1");
        existing.setName("Velho");
        existing.setEmail("old@mail");
        existing.setCategoria("VelhaCat");
        existing.setContato("11111");
        existing.setStatus(true);

        when(fornecedorRepository.findById("1")).thenReturn(Optional.of(existing));
        when(fornecedorRepository.save(any(Fornecedor.class))).thenAnswer(i -> i.getArgument(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(put("/uforn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        ArgumentCaptor<Fornecedor> cap = ArgumentCaptor.forClass(Fornecedor.class);
        verify(fornecedorRepository).save(cap.capture());
        Fornecedor saved = cap.getValue();
        assert saved.getName().equals("NomeNovo");
        assert saved.getEmail().equals("novo@mail");
        assert saved.getCategoria().equals("NovaCat");
        assert saved.getContato().equals("99999");
        assert saved.getStatus().equals(false);
    }

    @Test
    void testGetFornecedor_NotFound() throws Exception {
        when(fornecedorRepository.findById("404")).thenReturn(Optional.empty());
        mockMvc.perform(get("/gforn").param("id", "404"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetFornecedorByCategory() throws Exception {
        Fornecedor f = new Fornecedor();
        f.setId("1");
        f.setName("N");
        f.setCategoria("CatA");
        when(fornecedorRepository.findByCategoria("CatA")).thenReturn(Arrays.asList(f));

        mockMvc.perform(get("/gfornc").param("category", "CatA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].categoria").value("CatA"));
    }

    @Test
    void testDeleteFornecedor_NotFound() throws Exception {
        when(fornecedorRepository.findById("X")).thenReturn(Optional.empty());
        mockMvc.perform(delete("/dforn").param("id", "X"))
            .andExpect(status().isNotFound());
    }
    
    // Registra o módulo do JavaTime para o ObjectMapper
    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Isso registra o módulo do JavaTime
    }
}

