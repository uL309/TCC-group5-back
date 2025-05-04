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
    public ResponseEntity<String> UpdateEngenheiro(@RequestBody @Valid UserDTO entity) {
        // FIX: getEngenheiro returns a ResponseEntity, not null if not found. Check user existence directly.
        User user = service.findById(entity.getID_Engenheiro());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        user.setName(entity.getNome_Engenheiro());
        user.setUsername(entity.getEmail_Engenheiro());
        if (entity.getSenha_Engenheiro() != null && !entity.getSenha_Engenheiro().isEmpty()) {
            String ePassword = new BCryptPasswordEncoder().encode(entity.getSenha_Engenheiro());
            user.setPassword(ePassword);
        }
        user.setRole(entity.getRole_Engenheiro());
        user.setStatus(entity.getStatus_Engenheiro());
        service.save(user);

        return ResponseEntity.ok().body("Engenheiro updated successfully");
    }

    @GetMapping("/ge")
    public ResponseEntity<UserDTO> getEngenheiro(@RequestParam String param, @RequestParam String role) {
        if (param == null || param.isEmpty() || role == null || role.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UserRole userRole = UserRole.fromRoleValue(Integer.parseInt(role));
        User user = service.findByIdAndRole(Integer.parseInt(param), userRole);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setNome_Engenheiro(user.getName());
        userDTO.setID_Engenheiro(user.getId());
        userDTO.setEmail_Engenheiro(user.getUsername());
        userDTO.setRole_Engenheiro(user.getRole());
        userDTO.setStatus_Engenheiro(user.getStatus());

        return ResponseEntity.ok().body(userDTO);
    }

    @GetMapping("/gel")
    public ResponseEntity<List<UserDTO>> getEngenheirolist(@RequestParam String param) {
        if (param == null || param.isEmpty()) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
        UserRole userRole = UserRole.fromRoleValue(Integer.parseInt(param));
        List<User> userList = service.findAllByRole(userRole);
        List<UserDTO> dto = userList.stream().map(user -> {
            UserDTO u = new UserDTO();
            u.setID_Engenheiro(user.getId());
            u.setNome_Engenheiro(user.getName());
            u.setEmail_Engenheiro(user.getUsername());
            u.setRole_Engenheiro(user.getRole());
            u.setStatus_Engenheiro(user.getStatus());
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
