package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;  // <-- adiciona assertTrue, etc.
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserDTO;
import puc.airtrack.airtrack.Login.UserRole;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.User.UserController;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;
    @Mock private UserService userService;
    @InjectMocks private UserController userController;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        // Monta o MockMvc sem carregar o contexto do Spring
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    /* ===================== CREATE USER ===================== */

    @Test
    void createUser_sucesso() throws Exception {
        UserDTO dto = baseDto();
        when(userService.findByUsername(dto.getUsername())).thenReturn(null);
        when(userService.newSave(any(User.class))).thenReturn(42);

        mockMvc.perform(post("/cre")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/user/get?id=42&role=" + dto.getRole()))
                .andExpect(content().string("User created successfully"));
    }

    @Test
    void createUser_usuarioJaExiste() throws Exception {
        UserDTO dto = baseDto();
        when(userService.findByUsername(dto.getUsername())).thenReturn(new User());

        mockMvc.perform(post("/cre")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User already exists"));
    }

    @Test
    void createUser_roleNula() throws Exception {
        UserDTO dto = baseDto();
        dto.setRole(null);
        when(userService.findByUsername(dto.getUsername())).thenReturn(null);

        mockMvc.perform(post("/cre")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid role"));
    }

    /* ===================== UPDATE USER ===================== */

    @Test
    void updateUser_usuarioNaoEncontrado() throws Exception {
        UserDTO dto = baseDto();
        dto.setId(99);
        when(userService.findById(99)).thenReturn(null);

        mockMvc.perform(put("/upe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }

    @Test
    void updateUser_camposBrancosNaoAlteram() throws Exception {
        User existente = new User();
        existente.setId(10);
        existente.setName("Original");
        existente.setUsername("orig@mail.com");
        existente.setPassword("ENC_OLD");
        existente.setRole(UserRole.ROLE_ADMIN);
        existente.setStatus(true);
        existente.setFirstAccess(true);
        existente.setCpf("123");

        when(userService.findById(10)).thenReturn(existente);

        UserDTO dto = new UserDTO();
        dto.setId(10);
        dto.setName("   ");
        dto.setUsername("");
        dto.setPassword("");
        dto.setRole(null);
        dto.setStatus(null);
        dto.setFirstAccess(null);
        dto.setCpf("");

        mockMvc.perform(put("/upe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully"));

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userService).save(cap.capture());
        User saved = cap.getValue();
        // Nenhum campo deve ter sido modificado
        assert(saved.getName().equals("Original"));
        assert(saved.getUsername().equals("orig@mail.com"));
        assert(saved.getPassword().equals("ENC_OLD"));
        assert(saved.getRole() == UserRole.ROLE_ADMIN);
        assert(saved.getStatus().equals(true));
        assert(saved.getFirstAccess().equals(true));
        assert(saved.getCpf().equals("123"));
    }

    @Test
    void updateUser_camposAlterados() throws Exception {
        User existente = new User();
        existente.setId(11);
        existente.setName("N1");
        existente.setUsername("u1@mail.com");
        existente.setPassword("OLD");
        existente.setRole(UserRole.ROLE_ENGENHEIRO);
        existente.setStatus(false);
        existente.setFirstAccess(false);
        existente.setCpf("000");

        when(userService.findById(11)).thenReturn(existente);

        UserDTO dto = new UserDTO();
        dto.setId(11);
        dto.setName("Novo Nome");
        dto.setUsername("novo@mail.com");
        dto.setPassword("NovaSenha");
        dto.setRole(UserRole.ROLE_SUPERVISOR);
        dto.setStatus(true);
        dto.setFirstAccess(true);
        dto.setCpf("11122233344");

        mockMvc.perform(put("/upe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userService).save(cap.capture());
        User saved = cap.getValue();
        assert(saved.getName().equals("Novo Nome"));
        assert(saved.getUsername().equals("novo@mail.com"));
        assert(!saved.getPassword().equals("OLD")); // foi re-encodada
        assert(saved.getRole() == UserRole.ROLE_SUPERVISOR);
        assert(saved.getStatus());
        assert(saved.getFirstAccess());
        assert(saved.getCpf().equals("11122233344"));
    }

    /* ===================== GET USER ===================== */

    @Test
    void getUser_encontrado() throws Exception {
        User u = new User();
        u.setId(5);
        u.setName("Teste");
        u.setUsername("t@mail.com");
        u.setRole(UserRole.ROLE_AUDITOR);
        u.setStatus(true);
        u.setFirstAccess(false);
        u.setCpf("123");

        when(userService.findByIdAndRole(5, UserRole.ROLE_AUDITOR)).thenReturn(u);

        var res = mockMvc.perform(get("/ge")
                .param("id","5")
                .param("role","ROLE_AUDITOR")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andReturn();

        String body = res.getResponse().getContentAsString();
        assertTrue(body.contains("t@mail.com")); // verifica conteúdo bruto
        assertTrue(body.contains("\"nome\":\"Teste\"") || body.contains("\"name\":\"Teste\""));
    }

    @Test
    void getUser_naoEncontrado() throws Exception {
        when(userService.findByIdAndRole(7, UserRole.ROLE_ADMIN)).thenReturn(null);
        mockMvc.perform(get("/ge").param("id","7").param("role","ROLE_ADMIN"))
                .andExpect(status().isNotFound());
    }

    /* ===================== GET USER LIST ===================== */

    @Test
    void getUserList_vazio() throws Exception {
        when(userService.findAllByRole(UserRole.ROLE_SUPERVISOR)).thenReturn(List.of());
        mockMvc.perform(get("/gel")
                .param("role","ROLE_SUPERVISOR")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getUserList_comItens() throws Exception {
        User u = new User();
        u.setId(9); u.setName("NomeX"); u.setUsername("x@mail.com");
        u.setRole(UserRole.ROLE_SUPERVISOR); u.setStatus(true); u.setFirstAccess(true); u.setCpf("999");
        when(userService.findAllByRole(UserRole.ROLE_SUPERVISOR)).thenReturn(List.of(u));

        var res = mockMvc.perform(get("/gel")
                .param("role","ROLE_SUPERVISOR")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andReturn();

        assertTrue(res.getResponse().getContentAsString().contains("x@mail.com"));
    }

    /* ===================== DELETE USER ===================== */

    @Test
    void deleteUser_sucessoSoftDelete() throws Exception {
        User u = new User(); u.setId(15); u.setStatus(true);
        when(userService.findById(15)).thenReturn(u);

        mockMvc.perform(delete("/de").param("id","15"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userService).save(cap.capture());
        assert(cap.getValue().getStatus() == false);
    }

    @Test
    void deleteUser_usuarioJaInativo() throws Exception {
        User u = new User(); u.setId(16); u.setStatus(false);
        when(userService.findById(16)).thenReturn(u);

        mockMvc.perform(delete("/de").param("id","16"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
        verify(userService, never()).save(any());
    }

    @Test
    void deleteUser_usuarioNull() throws Exception {
        when(userService.findById(99)).thenReturn(null);
        mockMvc.perform(delete("/de").param("id","99"))
                .andExpect(status().isNotFound());
    }

    /* ===================== USER STATS ===================== */

    @Test
    void getUserStats_cobreTodosOsFiltros() throws Exception {
        User ativoEng = mkUser(1, true, UserRole.ROLE_ENGENHEIRO);
        User ativoAud = mkUser(2, true, UserRole.ROLE_AUDITOR);
        User ativoSup = mkUser(3, true, UserRole.ROLE_SUPERVISOR);
        User ativoAdm = mkUser(4, true, UserRole.ROLE_ADMIN);
        User inativoEng = mkUser(5, false, UserRole.ROLE_ENGENHEIRO);
        User statusNullRoleNull = mkUser(6, null, null);

        ArrayList<User> lista = new ArrayList<>(Arrays.asList(
                ativoEng, ativoAud, ativoSup, ativoAdm, inativoEng, statusNullRoleNull
        ));
        when(userService.findAll()).thenReturn(lista);

        var mvcRes = mockMvc.perform(get("/admin/users/stats")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String body = mvcRes.getResponse().getContentAsString();
        assertTrue(body.contains("6"));
        assertTrue(body.contains("4"));
        assertTrue(body.contains("1"));
    }

    /* ===================== HELPERS ===================== */

    private UserDTO baseDto() {
        UserDTO dto = new UserDTO();
        dto.setName("NomeTeste");
        dto.setCpf("06547000010");
        dto.setPassword("SenhaTeste");
        dto.setRole(UserRole.ROLE_ENGENHEIRO);
        dto.setStatus(true);
        dto.setFirstAccess(true);
        dto.setUsername("emailteste@mail.com");
        return dto;
    }

    private User mkUser(int id, Boolean status, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setStatus(status);
        u.setRole(role);
        u.setName("U"+id);
        u.setUsername("u"+id+"@mail.com");
        u.setFirstAccess(true);
        u.setCpf("000");
        return u;
    }
}

