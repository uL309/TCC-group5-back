package puc.airtrack.airtrack;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
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
public class FornecedorControllerTeste {
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
    
    // Registra o módulo do JavaTime para o ObjectMapper
    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Isso registra o módulo do JavaTime
    }
}
