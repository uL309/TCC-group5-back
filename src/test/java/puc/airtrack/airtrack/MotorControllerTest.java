package puc.airtrack.airtrack;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
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

import puc.airtrack.airtrack.Cliente.ClienteRepo;
import puc.airtrack.airtrack.Motor.Motor;
import puc.airtrack.airtrack.Motor.MotorController;
import puc.airtrack.airtrack.Motor.MotorDTO;
import puc.airtrack.airtrack.Motor.MotorRepository;
import puc.airtrack.airtrack.tipoMotor.TipoMotor;
import puc.airtrack.airtrack.tipoMotor.TipoMotorRepository;
@WebMvcTest(MotorController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MotorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityFilter securityFilter;

    @MockBean
    private TokenService tokenService;
    
    @MockBean
    private MotorRepository motorRepository;

    @MockBean
    private TipoMotorRepository tipoMotorRepository;

    @MockBean
    private ClienteRepo clienteRepository;

    @Test
    void testCreateMotor() throws Exception {
        MotorDTO dto = new MotorDTO();
        dto.setMarca("MarcaTeste");
        dto.setModelo("ModeloTeste");
        dto.setSerie_motor("SERIE123");
        dto.setData_cadastro(LocalDate.now());
        dto.setStatus(true);
        dto.setCliente_cpf("06547000010");
        dto.setHoras_operacao(5);

        when(motorRepository.save(any(Motor.class))).thenAnswer(i -> {
            Motor m = i.getArgument(0);
            m.setId(1);
            return m;
        });

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(post("/cmotor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
    
    @Test
    void testGetAllMotores() throws Exception {
        Motor motor = new Motor();
        TipoMotor tipoMotor = new TipoMotor();
        tipoMotor.setTbo(100);
        motor.setId(1);
        motor.setMarca("Rolls-Royce");
        motor.setModelo("Rolls-Royce Trent 1000");
        motor.setSerie_motor("SERIE123");
        motor.setStatus(true);
        motor.setHoras_operacao(5);

        List<Motor> motores = Arrays.asList(motor);
        when(motorRepository.findAll()).thenReturn(motores);

        mockMvc.perform(get("/gmotores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].marca").value("Rolls-Royce"));
    }
@Test
void testBuscarPorId_MotorEncontrado() throws Exception {
    Motor motor = new Motor();
    motor.setId(1);
    motor.setMarca("Rolls-Royce");
    motor.setModelo("Trent 1000");
    motor.setSerie_motor("SERIE123");
    motor.setStatus(true);
    motor.setHoras_operacao(5);

    when(motorRepository.findById(1)).thenReturn(Optional.of(motor));

    mockMvc.perform(get("/gmotor").param("param", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.marca").value("Rolls-Royce"));
}

@Test
void testBuscarPorId_MotorNaoEncontrado() throws Exception {
    when(motorRepository.findById(99)).thenReturn(Optional.empty());

    mockMvc.perform(get("/gmotor").param("param", "99"))
        .andExpect(status().isNotFound());
}
    @Test
    void testUpdateMotor() throws Exception {
        MotorDTO dto = new MotorDTO();
        dto.setId(1);
        dto.setMarca("Rolls-Royce");
        dto.setModelo("Rolls-Royce Trent 1000");
        dto.setSerie_motor("SERIE123");
        dto.setData_cadastro(LocalDate.now());
        dto.setStatus(true);
        dto.setHoras_operacao(10);

        Motor motor = new Motor();
        motor.setId(1);

        when(motorRepository.findById(1)).thenReturn(Optional.of(motor));
        when(motorRepository.save(any(Motor.class))).thenReturn(motor);

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(put("/umotor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteMotor() throws Exception {
        Motor motor = new Motor();
        motor.setId(1);

        when(motorRepository.findById(1)).thenReturn(Optional.of(motor));

        mockMvc.perform(delete("/dmotor").param("param", "1"))
    .andExpect(status().isOk());
    }
    
    // Registra o módulo do JavaTime para o ObjectMapper
    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Isso registra o módulo do JavaTime
    }
}

