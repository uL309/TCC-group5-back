package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserRole;
import puc.airtrack.airtrack.Login.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private Repositorio repositorio;

    @InjectMocks
    private UserService userService;

    private User sample;

    @BeforeEach
    void setup() {
        sample = new User();
        sample.setId(42);
        sample.setUsername("u1");
        sample.setCpf("00011122233");
        // ajuste outros campos se necessário
    }

    @Test
    void findByUsername_returnsUser() {
        when(repositorio.findByUsername("u1")).thenReturn(sample);
        User u = userService.findByUsername("u1");
        assertNotNull(u);
        assertEquals("u1", u.getUsername());
    }

    @Test
    void findById_returnsUser() {
        when(repositorio.findById(42)).thenReturn(sample);
        User u = userService.findById(42);
        assertNotNull(u);
        assertEquals(42, u.getId());
    }

    @Test
    void findByUsernameAndPassword_returnsUser() {
        when(repositorio.findByUsernameAndPassword("u1", "p")).thenReturn(sample);
        User u = userService.findByUsernameAndPassword("u1", "p");
        assertNotNull(u);
    }

    @Test
    void save_delegatesToRepository() {
        // repositorio.save(...) retorna um User — stub para retornar o mesmo objeto
        when(repositorio.save(sample)).thenReturn(sample);
        userService.save(sample);
        verify(repositorio).save(sample);
    }

    @Test
    void newSave_returnsGeneratedId() {
        User saved = new User();
        saved.setId(99);
        when(repositorio.save(sample)).thenReturn(saved);
        int id = userService.newSave(sample);
        assertEquals(99, id);
    }

    @Test
    void findByIdAndStatus_delegates() {
        when(repositorio.findByIdAndStatus(42, true)).thenReturn(sample);
        User u = userService.findByIdAndStatus(42, true);
        assertSame(sample, u);
    }

    @Test
    void findByIdAndRole_delegates() {
        when(repositorio.findByIdAndRole(42, UserRole.ROLE_ENGENHEIRO)).thenReturn(sample);
        User u = userService.findByIdAndRole(42, UserRole.ROLE_ENGENHEIRO);
        assertSame(sample, u);
    }

    @Test
    void findAll_returnsList() {
        ArrayList<User> list = new ArrayList<>();
        list.add(sample);
        when(repositorio.findAll()).thenReturn(list);
        ArrayList<User> result = userService.findAll();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findAllByRole_returnsList() {
        List<User> list = new ArrayList<>();
        list.add(sample);
        when(repositorio.findAllByRole(UserRole.ROLE_SUPERVISOR)).thenReturn(list);
        List<User> res = userService.findAllByRole(UserRole.ROLE_SUPERVISOR);
        assertEquals(1, res.size());
    }

    @Test
    void findByCpf_returnsUser() {
        when(repositorio.findByCpf("00011122233")).thenReturn(sample);
        User u = userService.findByCpf("00011122233");
        assertNotNull(u);
        assertEquals("00011122233", u.getCpf());
    }
}
