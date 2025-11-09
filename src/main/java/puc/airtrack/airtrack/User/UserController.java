package puc.airtrack.airtrack.User;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserDTO;
import puc.airtrack.airtrack.Login.UserRole;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.User.UserStatsDTO;

@Controller
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("/cre")
    public ResponseEntity<String> createUser(@RequestBody @Valid UserDTO entity) {
        if (service.findByUsername(entity.getUsername()) != null) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        if (entity.getRole() == null) {
            return ResponseEntity.badRequest().body("Invalid role");
        }
        String encodedPassword = new BCryptPasswordEncoder().encode(entity.getPassword());
        User user = new User();
        user.setName(entity.getName());
        user.setUsername(entity.getUsername());
        user.setPassword(encodedPassword);
        user.setStatus(entity.getStatus());
        user.setRole(entity.getRole());
        user.setFirstAccess(entity.getFirstAccess());
        user.setCpf(entity.getCpf());
        user.setId(service.newSave(user));
        URI location = URI.create("/user/get?id=" + user.getId() + "&role=" + user.getRole());
        return ResponseEntity.created(location).body("User created successfully");
    }

    @PutMapping("/upe")
    public ResponseEntity<String> updateUser(@RequestBody @Valid UserDTO entity) {
        User user = service.findById(entity.getId());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        if (entity.getName() != null && !entity.getName().isBlank()) {
            user.setName(entity.getName());
        }
        if (entity.getUsername() != null && !entity.getUsername().isBlank()) {
            user.setUsername(entity.getUsername());
        }
        String password = entity.getPassword();
        if (password != null && !password.isBlank()) {
            user.setPassword(new BCryptPasswordEncoder().encode(password));
        }
        if (entity.getRole() != null) {
            user.setRole(entity.getRole());
        }
        if (entity.getStatus() != null) {
            user.setStatus(entity.getStatus());
        }
        if (entity.getFirstAccess() != null) {
            user.setFirstAccess(entity.getFirstAccess());
        }
        if (entity.getCpf() != null && !entity.getCpf().isBlank()) {
            user.setCpf(entity.getCpf());
        }
        service.save(user);
        return ResponseEntity.ok("User updated successfully");
    }

    @GetMapping("/ge")
    public ResponseEntity<UserDTO> getUser(@RequestParam int id, @RequestParam UserRole role) {
        User user = service.findByIdAndRole(id, role);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setUsername(user.getUsername());
        userDTO.setRole(user.getRole());
        userDTO.setStatus(user.getStatus());
        userDTO.setFirstAccess(user.getFirstAccess());
        userDTO.setCpf(user.getCpf());
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/gel")
    public ResponseEntity<List<UserDTO>> getUserList(@RequestParam UserRole role) {
        List<User> userList = service.findAllByRole(role);
        List<UserDTO> dtoList = userList.stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setUsername(user.getUsername());
            dto.setRole(user.getRole());
            dto.setStatus(user.getStatus());
            dto.setFirstAccess(user.getFirstAccess());
            dto.setCpf(user.getCpf());
            return dto;
        }).toList();
        return ResponseEntity.ok(dtoList);
    }

    @DeleteMapping("/de")
    public ResponseEntity<String> deleteUser(@RequestParam int id) {
        User user = service.findById(id);
        if (user != null && Boolean.TRUE.equals(user.getStatus())) {
            user.setStatus(false); // Soft delete
            service.save(user);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    @GetMapping("/admin/users/stats")
    public ResponseEntity<UserStatsDTO> getUserStats() {
        List<User> todosUsuarios = service.findAll();
        
        long totalUsuarios = todosUsuarios.size();
        long usuariosAtivos = todosUsuarios.stream()
            .filter(u -> u.getStatus() != null && u.getStatus())
            .count();
        
        long usuariosEngenheiro = todosUsuarios.stream()
            .filter(u -> u.getRole() != null && u.getRole() == UserRole.ROLE_ENGENHEIRO && u.getStatus() != null && u.getStatus())
            .count();
        
        long usuariosAuditor = todosUsuarios.stream()
            .filter(u -> u.getRole() != null && u.getRole() == UserRole.ROLE_AUDITOR && u.getStatus() != null && u.getStatus())
            .count();
        
        long usuariosSupervisor = todosUsuarios.stream()
            .filter(u -> u.getRole() != null && u.getRole() == UserRole.ROLE_SUPERVISOR && u.getStatus() != null && u.getStatus())
            .count();
        
        long usuariosAdmin = todosUsuarios.stream()
            .filter(u -> u.getRole() != null && u.getRole() == UserRole.ROLE_ADMIN && u.getStatus() != null && u.getStatus())
            .count();
        
        UserStatsDTO stats = new UserStatsDTO();
        stats.setTotalUsuarios((int) totalUsuarios);
        stats.setUsuariosAtivos((int) usuariosAtivos);
        stats.setUsuariosEngenheiro((int) usuariosEngenheiro);
        stats.setUsuariosAuditor((int) usuariosAuditor);
        stats.setUsuariosSupervisor((int) usuariosSupervisor);
        stats.setUsuariosAdmin((int) usuariosAdmin);
        
        return ResponseEntity.ok(stats);
    }
}
