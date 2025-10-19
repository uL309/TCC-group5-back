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

import puc.airtrack.airtrack.OrdemDeServico.CabecalhoOrdemRepository;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdem;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemController;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemDTO;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemRepository;
import puc.airtrack.airtrack.OrdemDeServico.LinhaOrdemService;
import puc.airtrack.airtrack.Pecas.PecasRepository;

@WebMvcTest(LinhaOrdemController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LinhaOrdemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LinhaOrdemRepository linhaOrdemRepository;

    @MockBean
    private CabecalhoOrdemRepository cabecalhoOrdemRepository;

    @MockBean
    private PecasRepository pecasRepository;

    @MockBean
    private puc.airtrack.airtrack.Login.UserService userService;

    @MockBean
    private LinhaOrdemService linhaOrdemService;

    @MockBean
    private puc.airtrack.airtrack.TokenService tokenService;

    @MockBean
    private puc.airtrack.airtrack.SecurityFilter securityFilter;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testCreateLinhaOrdem_Success() throws Exception {
        LinhaOrdemDTO dto = new LinhaOrdemDTO();
        dto.setQuantidade(new java.util.ArrayList<>(java.util.Arrays.asList(2)));
        dto.setTempoGasto(1.5f);

        LinhaOrdemDTO saved = new LinhaOrdemDTO();
        saved.setId(10);
        saved.setQuantidade(new java.util.ArrayList<>(java.util.Arrays.asList(2)));
        saved.setTempoGasto(1.5f);

        when(linhaOrdemService.create(any(LinhaOrdemDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/linhaordem/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/linhaordem/get?id=10"));
    }

    @Test
    void testCreateLinhaOrdem_BadRequest() throws Exception {
        LinhaOrdemDTO dto = new LinhaOrdemDTO();
        when(linhaOrdemService.create(any(LinhaOrdemDTO.class))).thenThrow(new IllegalArgumentException("bad"));
        mockMvc.perform(post("/linhaordem/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateLinhaOrdem_Success() throws Exception {
        LinhaOrdemDTO dto = new LinhaOrdemDTO();
        dto.setId(5);
        dto.setQuantidade(new java.util.ArrayList<>(java.util.Arrays.asList(3)));
        dto.setTempoGasto(2.0f);
        dto.setOrdemId(1);
        dto.setPecaId(1);
        dto.setEngenheiroId("2");

        LinhaOrdem entity = new LinhaOrdem();
        entity.setId(5);

        when(linhaOrdemRepository.findById(5)).thenReturn(Optional.of(entity));
        when(cabecalhoOrdemRepository.findById(1)).thenReturn(Optional.empty());
        when(pecasRepository.findById(1)).thenReturn(Optional.empty());
        when(userService.findById(2)).thenReturn(null); // service may return null, controller tolerates
        when(linhaOrdemRepository.save(any(LinhaOrdem.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/linhaordem/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test
    void testUpdateLinhaOrdem_NotFound() throws Exception {
        LinhaOrdemDTO dto = new LinhaOrdemDTO();
        dto.setId(99);
        when(linhaOrdemRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/linhaordem/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateLinhaOrdem_InvalidEngenheiroId() throws Exception {
        LinhaOrdemDTO dto = new LinhaOrdemDTO();
        dto.setId(6);
        dto.setEngenheiroId("not-a-number");

        LinhaOrdem entity = new LinhaOrdem(); entity.setId(6);
        when(linhaOrdemRepository.findById(6)).thenReturn(Optional.of(entity));
        when(linhaOrdemRepository.save(any(LinhaOrdem.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/linhaordem/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
        // if parsing fails controller sets engenheiro null and still saves
    }

    @Test
    void testGetLinhaOrdem_FoundAndNotFound() throws Exception {
        LinhaOrdemDTO dto = new LinhaOrdemDTO();
        dto.setId(7);
        when(linhaOrdemService.findDtoById(7)).thenReturn(Optional.of(dto));
        mockMvc.perform(get("/linhaordem/get").param("id", "7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(7));

        when(linhaOrdemService.findDtoById(8)).thenReturn(Optional.empty());
        mockMvc.perform(get("/linhaordem/get").param("id", "8"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllLinhaOrdem() throws Exception {
        LinhaOrdemDTO d1 = new LinhaOrdemDTO(); d1.setId(1);
        LinhaOrdemDTO d2 = new LinhaOrdemDTO(); d2.setId(2);
        when(linhaOrdemService.findAllDto()).thenReturn(Arrays.asList(d1, d2));
        mockMvc.perform(get("/linhaordem/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void testDeleteLinhaOrdem_FoundAndNotFound() throws Exception {
        LinhaOrdem e = new LinhaOrdem(); e.setId(3);
        when(linhaOrdemRepository.findById(3)).thenReturn(Optional.of(e));
        mockMvc.perform(delete("/linhaordem/delete").param("id", "3"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("deleted")));
        verify(linhaOrdemRepository).deleteById(3);

        when(linhaOrdemRepository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/linhaordem/delete").param("id", "99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetByCabecalho() throws Exception {
        LinhaOrdemDTO d = new LinhaOrdemDTO(); d.setId(4);
        when(linhaOrdemService.findByCabecalhoId(10)).thenReturn(Arrays.asList(d));
        mockMvc.perform(get("/linhaordem/bycabecalho").param("cabecalhoId", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(4));
    }
}
