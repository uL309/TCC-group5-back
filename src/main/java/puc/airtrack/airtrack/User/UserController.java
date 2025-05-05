package puc.airtrack.airtrack.User;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserDTO;
import puc.airtrack.airtrack.Login.UserRole;
import puc.airtrack.airtrack.Login.UserService;



@Controller
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("/cre")
    public ResponseEntity<String> createEngenheiro(@RequestBody @Valid UserDTO entity) {
        if (this.service.findByUsername(entity.getEmail_Engenheiro()) != null) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        if (entity.getRole_Engenheiro() == null) {
            return ResponseEntity.badRequest().body("Invalid role");
        }
        String ePassword = new BCryptPasswordEncoder().encode(entity.getSenha_Engenheiro());
        User user = new User();
        user.setName(entity.getNome_Engenheiro());
        user.setUsername(entity.getEmail_Engenheiro());
        user.setPassword(ePassword);
        user.setStatus(entity.getStatus_Engenheiro());
        user.setRole(entity.getRole_Engenheiro());
        user.setSalario(entity.getSalario_Engenheiro());
        user.setId(service.newSave(user));
        URI location;
        switch (user.getRole()) {
            case ROLE_ENGENHEIRO:
                location = URI.create("/ge?param=" + user.getId());
                break;
            case ROLE_AUDITOR:
                location = URI.create("/gau?param=" + user.getId());
                break;
            case ROLE_SUPERVISOR:
                location = URI.create("/gs?param=" + user.getId());
                break;
            case ROLE_ADMIN:
                location = URI.create("/ga?param=" + user.getId());
                break;
            default:
                return ResponseEntity.badRequest().body("Unknown role");
        }
        return ResponseEntity.created(location).body("User created successfully");
    }

    @PutMapping("/upe")
    public ResponseEntity<String> updateEngenheiro(@RequestBody @Valid UserDTO entity) {
        User user = service.findById(entity.getID_Engenheiro());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        if (entity.getNome_Engenheiro() != null && !entity.getNome_Engenheiro().isBlank()) {
            user.setName(entity.getNome_Engenheiro());
        }

        if (entity.getEmail_Engenheiro() != null && !entity.getEmail_Engenheiro().isBlank()) {
            user.setUsername(entity.getEmail_Engenheiro());
        }

        String password = entity.getSenha_Engenheiro();
        if (password != null && !password.isBlank()) {
            user.setPassword(new BCryptPasswordEncoder().encode(password));
        }

        if (entity.getRole_Engenheiro() != null) {
            user.setRole(entity.getRole_Engenheiro());
        }

        if (entity.getStatus_Engenheiro() != null) {
            user.setStatus(entity.getStatus_Engenheiro());
        }

        if (entity.getSalario_Engenheiro() > 0) {
            user.setSalario(entity.getSalario_Engenheiro());
        }

        service.save(user);
        return ResponseEntity.ok().body("Engenheiro updated successfully");
    }


    @GetMapping("/ge")
    public ResponseEntity<UserDTO> getEngenheiro(@RequestParam String param, @RequestParam UserRole role) {
        if (param == null || param.isEmpty() || role == null || role.toString().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User user = service.findByIdAndRole(Integer.parseInt(param), role);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setNome_Engenheiro(user.getName());
        userDTO.setID_Engenheiro(user.getId());
        userDTO.setEmail_Engenheiro(user.getUsername());
        userDTO.setRole_Engenheiro(user.getRole());
        userDTO.setStatus_Engenheiro(user.getStatus());
        userDTO.setSalario_Engenheiro(user.getSalario());

        return ResponseEntity.ok().body(userDTO);
    }

    @GetMapping("/gel")
    public ResponseEntity<List<UserDTO>> getEngenheirolist(@RequestParam UserRole param) {
        if (param == null || param.toString().isEmpty()) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
        List<User> userList = service.findAllByRole(param);
        List<UserDTO> dto = userList.stream().map(user -> {
            UserDTO u = new UserDTO();
            u.setID_Engenheiro(user.getId());
            u.setNome_Engenheiro(user.getName());
            u.setEmail_Engenheiro(user.getUsername());
            u.setRole_Engenheiro(user.getRole());
            u.setStatus_Engenheiro(user.getStatus());
            u.setSalario_Engenheiro(user.getSalario());
            return u;
        }).toList();

        return ResponseEntity.ok(dto);
    }
    
    
    @PostMapping("/de")
    public ResponseEntity<String> deleteEngenheiro(@RequestParam String param) {
        User user = service.findById(Integer.parseInt(param));
        if (user != null && user.getStatus()) {
            user.setStatus(false); // Set status to false instead of deleting
            service.save(user);
        } else {
            return ResponseEntity.status(404).body("Engenheiro not found");
        }

        return ResponseEntity.ok().body("Engenheiro deleted successfully");
    }
}
