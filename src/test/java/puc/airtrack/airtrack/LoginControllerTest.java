package puc.airtrack.airtrack;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import puc.airtrack.airtrack.Login.FirstAccessRequest;
import puc.airtrack.airtrack.Login.LoginController;
import puc.airtrack.airtrack.Login.LoginDTO;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserDTO;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.services.PasswordResetService;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordResetService passwordResetService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void postLogin_success_returnsNameAndToken() throws Exception {
        LoginDTO req = new LoginDTO();
        req.setUsername("u");
        req.setPassword("p");

        User user = new User();
        user.setName("The Name");
        user.setUsername("u");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenService.generateToken(user)).thenReturn("the-token");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("the-token")))
            .andExpect(content().string(containsString("The Name")));
    }

    @Test
    void postRegister_success_returnsMessage() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setUsername("newuser");
        dto.setPassword("pass");
        dto.setRole(null);
        dto.setStatus(null);
        dto.setName("Name");
        dto.setFirstAccess(Boolean.TRUE);
        dto.setCpf("123");

        // userService.save is void - mock to do nothing
        doNothing().when(userService).save(any(User.class));

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("User registered successfully: newuser")));
    }

    @Test
    void resetPassword_callsService_andReturnsOk() throws Exception {
        doNothing().when(passwordResetService).resetPassword("u1");

        mockMvc.perform(post("/reset-password").param("username", "u1"))
            .andExpect(status().isOk());

        verify(passwordResetService).resetPassword("u1");
    }

    @Test
    void updatePasswordOnFirstAccess_userNotFound_returns404() throws Exception {
        FirstAccessRequest req = new FirstAccessRequest();
        req.setCpf("notfound");
        req.setNewPassword("x");
        when(userService.findByCpf("notfound")).thenReturn(null);

        mockMvc.perform(put("/first-access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("User not found")));
    }

    @Test
    void updatePasswordOnFirstAccess_blankPassword_returnsBadRequest() throws Exception {
        User u = new User(); u.setCpf("123");
        when(userService.findByCpf("123")).thenReturn(u);

        FirstAccessRequest req = new FirstAccessRequest();
        req.setCpf("123");
        req.setNewPassword(""); // blank

        mockMvc.perform(put("/first-access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.isEmptyString()));
    }

    @Test
    void updatePasswordOnFirstAccess_success_updatesPasswordAndSaves() throws Exception {
        User u = new User();
        u.setCpf("321");
        u.setFirstAccess(Boolean.TRUE);
        when(userService.findByCpf("321")).thenReturn(u);
        doNothing().when(userService).save(any(User.class));

        FirstAccessRequest req = new FirstAccessRequest();
        req.setCpf("321");
        req.setNewPassword("newPass123");

        mockMvc.perform(put("/first-access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Password updated successfully")));

        verify(userService).save(argThat(saved -> saved.getFirstAccess() != null && saved.getFirstAccess().equals(Boolean.FALSE)));
    }
}

