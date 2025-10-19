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

import puc.airtrack.airtrack.Cliente.Cliente;
import puc.airtrack.airtrack.Cliente.ClienteController;
import puc.airtrack.airtrack.Cliente.ClienteDTO;
import puc.airtrack.airtrack.Cliente.ClienteRepo;

@WebMvcTest(controllers = ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ClienteControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityFilter securityFilter;

    @MockBean
    private TokenService tokenService;
    @MockBean
    private ClienteRepo clienteRepository;




    @Test
    void testCreateCliente() throws Exception {
        ClienteDTO dto = new ClienteDTO();
        dto.setCpf("40654700010");
        dto.setName("NomeTeste");
        dto.setEmail("emailteste@mail.com");
        dto.setContato("41999999999");
        dto.setStatus(true);

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> {
            Cliente m = i.getArgument(0);
            m.setId(1);
            return m;
        });

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(post("/ccli")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
    
    @Test
    void testGetAllClientes() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setId(1);
        cliente.setCpf("40654700010");
        cliente.setName("NomeTeste");
        cliente.setEmail("emailteste@mail.com");
        cliente.setContato("41999999999");
        cliente.setStatus(true);

        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findAll()).thenReturn(clientes);

        mockMvc.perform(get("/gclis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("NomeTeste"));
    }
@Test
void testBuscarPorId_ClienteEncontrado() throws Exception {
    Cliente cliente = new Cliente();
    cliente.setId(1);
    cliente.setCpf("40654700010");
    cliente.setName("NomeTeste");
    cliente.setEmail("emailteste@mail.com");
    cliente.setContato("41999999999");
    cliente.setStatus(true);

    when(clienteRepository.findByCpf("40654700010")).thenReturn(Optional.of(cliente));

    mockMvc.perform(get("/gcli").param("id", "40654700010"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("1"))
        .andExpect(jsonPath("$.name").value("NomeTeste"));
}

@Test
void testBuscarPorId_FornecedorNaoEncontrado() throws Exception {
    when(clienteRepository.findByCpf("40654700010")).thenReturn(Optional.empty());

    mockMvc.perform(get("/gmotor").param("param", "99"))
        .andExpect(status().isNotFound());
}
    @Test
    void testUpdateCliente() throws Exception {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(2);
        dto.setCpf("84104788031");
        dto.setName("NomeAtualizado");
        dto.setEmail("emailAtualizado@gmail.com");
        dto.setContato("41988888888");
        dto.setStatus(true);
        Cliente cliente = new Cliente();
        cliente.setId(2);

        when(clienteRepository.findByCpf("84104788031")).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(put("/ucli")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
void testDeleteCliente() throws Exception {
    Cliente cliente = new Cliente();
    cliente.setId(1);
    cliente.setCpf("40654700010");

    when(clienteRepository.findByCpf("40654700010")).thenReturn(Optional.of(cliente));

    mockMvc.perform(get("/dcli").param("id", "40654700010"))
        .andExpect(status().isOk());
}
    
    // Registra o módulo do JavaTime para o ObjectMapper
    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Isso registra o módulo do JavaTime
    }
}

