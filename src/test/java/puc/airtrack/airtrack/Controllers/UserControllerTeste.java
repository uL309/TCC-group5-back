package puc.airtrack.airtrack.Controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import puc.airtrack.airtrack.Repositorio;
import puc.airtrack.airtrack.SecurityFilter;
import puc.airtrack.airtrack.TokenService;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserDTO;
import puc.airtrack.airtrack.Login.UserRole;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.User.UserController;
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTeste {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityFilter securityFilter;

    @MockBean
    private TokenService tokenService;
    
    @MockBean
    private UserService userService;

    @MockBean
    private Repositorio repositorio;

 

    @InjectMocks
    private UserController controller;

    @Test
    void testCreateUser() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setName("NomeTeste");
        dto.setCpf("06547000010");
        dto.setPassword("SenhaTeste");
        dto.setRole(UserRole.ROLE_ENGENHEIRO);
        dto.setStatus(true);
        dto.setFirstAccess(true);
        dto.setUsername("emailteste@mail.com");

        when(repositorio.save(any(puc.airtrack.airtrack.Login.User.class))).thenAnswer(i -> {
    puc.airtrack.airtrack.Login.User u = i.getArgument(0);
    u.setId(1);
    return u;
});
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(post("/cre")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
    
    @Test
    void testGetAllUsers() throws Exception {
        User user = new User();
        user.setId(1);
        user.setName("NomeTeste");
        user.setCpf("06547000010");
        user.setPassword("SenhaTeste");
        user.setRole(UserRole.ROLE_ADMIN);
        user.setStatus(true);
        user.setFirstAccess(true);
        user.setUsername("emailteste@mail.com");

        List<User> users = List.of(user);
when(userService.findAllByRole(UserRole.ROLE_ADMIN)).thenReturn(users);

        mockMvc.perform(get("/gel")
        .param("role", "ROLE_ADMIN"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].nome").value("NomeTeste"));
    }

@Test
void testBuscarPorId_UserEncontrado() throws Exception {
    User user = new User();
    user.setId(1);
    user.setName("NomeTeste");
    user.setCpf("06547000010");
    user.setPassword("SenhaTeste");
    user.setRole(UserRole.ROLE_ADMIN);
    user.setStatus(true);
    user.setFirstAccess(true);
    user.setUsername("emailteste@mail.com");

    when(userService.findByIdAndRole(1, UserRole.ROLE_ADMIN)).thenReturn(user); // ✅ mock correto

    mockMvc.perform(get("/ge")
            .param("id", "1")
            .param("role", "ROLE_ADMIN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("NomeTeste"));
}

@Test
void testBuscarPorId_MotorNaoEncontrado() throws Exception {
    when(repositorio.findById(99)).thenReturn(null);

    mockMvc.perform(get("/ge").param("id", "99").param("role", "ROLE_ADMIN"))
        .andExpect(status().isNotFound());
}

    @Test
    void testUpdateMotor() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setId(1);
    dto.setName("NomeTeste");
    dto.setCpf("06547000010");
    dto.setPassword("SenhaTeste");
    dto.setRole(UserRole.ROLE_ADMIN);
    dto.setStatus(true);
    dto.setFirstAccess(true);
    dto.setUsername("emailteste@mail.com");

        User motor = new User();
        motor.setId(1);

        when(userService.findById(1)).thenReturn(motor);
        when(repositorio.save(any(User.class))).thenReturn(motor);

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mockMvc.perform(put("/upe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

   @Test
void testDeleteUser() throws Exception {
    User user = new User();
    user.setId(1);
    user.setStatus(true); // ✅ necessário

    when(userService.findById(1)).thenReturn(user);

    mockMvc.perform(delete("/de").param("id", "1"))
            .andExpect(status().isOk());
}
    // Registra o módulo do JavaTime para o ObjectMapper
    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Isso registra o módulo do JavaTime
    }
}
