package puc.airtrack.airtrack.Engenheiro;


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
public class EngenheiroController {

    @Autowired
    private UserService service;
    
    @PostMapping("/cre")
    public ResponseEntity<String> createEngenheiro(@RequestBody @Valid UserDTO entity) {
        if (this.service.findByUsername(entity.getEmail_Engenheiro()) != null) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        String epassword = new BCryptPasswordEncoder().encode(entity.getSenha_Engenheiro());
        User user = new User();
        URI location = null;
        user.setName(entity.getNome_Engenheiro());
        user.setUsername(entity.getEmail_Engenheiro());
        user.setPassword(epassword);
        user.setStatus(entity.getStatus_Engenheiro());
        user.setId(service.newSave(user));
        
        if (user.getRole() == UserRole.ROLE_ENGENHEIRO) {
            location = URI.create("/ge?param=" + user.getId());
            user.setRole(UserRole.fromRoleValue(entity.getRole_Engenheiro()));
        }else if (user.getRole() == UserRole.ROLE_AUDITOR) {
            location = URI.create("/ga?param=" + user.getId());
            user.setRole(UserRole.fromRoleValue(entity.getRole_Engenheiro()));
        } else if (user.getRole() == UserRole.ROLE_SUPERVISOR) {
            location = URI.create("/gs?param=" + user.getId());
            user.setRole(UserRole.fromRoleValue(entity.getRole_Engenheiro()));
        } else if (user.getRole() == UserRole.ROLE_ADMIN) {
            location = URI.create("/ga?param=" + user.getId());
            user.setRole(UserRole.fromRoleValue(entity.getRole_Engenheiro()));
        } else {
            return ResponseEntity.badRequest().body("Invalid role");
        }
        
        return ResponseEntity.created(location).body("User created successfully");
    }
    
    @PutMapping("/upe")
    public String UpdateEngenheiro(@RequestBody @Valid UserDTO entity) {
        String epassword = new BCryptPasswordEncoder().encode(entity.getSenha_Engenheiro());
        if (getEngenheiro(entity.getID_Engenheiro().toString()) == null) {
            return ResponseEntity.badRequest().body("User not found").toString();
        }
        User user = service.findById(entity.getID_Engenheiro());
        user.setName(entity.getNome_Engenheiro());
        user.setUsername(entity.getEmail_Engenheiro());
        user.setPassword(epassword);
        user.setRole(UserRole.fromRoleValue(entity.getRole_Engenheiro()));
        user.setStatus(entity.getStatus_Engenheiro());
        service.save(user);

        return ResponseEntity.ok().body("Engenheiro updated successfully").toString();
    }

    @GetMapping("/ge")
    public ResponseEntity<UserDTO> getEngenheiro(@RequestParam String param) {
        User user = service.findByIdAndRole(Integer.parseInt(param), UserRole.ROLE_ENGENHEIRO.getRole());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        UserDTO userDTO = new UserDTO();
        
        userDTO.setNome_Engenheiro(user.getName());
        userDTO.setID_Engenheiro(user.getId()); 
        userDTO.setEmail_Engenheiro(user.getUsername());
        userDTO.setSenha_Engenheiro(user.getPassword());
        userDTO.setRole_Engenheiro(user.getRole().ordinal());
        userDTO.setStatus_Engenheiro(user.getStatus());

        return ResponseEntity.ok().body(userDTO);
    }

    @GetMapping("/gel")
    public ResponseEntity<List<UserDTO>> getEngenheirolist() {
        List<User> userList = service.findAllByRole(UserRole.ROLE_ENGENHEIRO);
        List<UserDTO> dto = userList.stream().map(user -> {
            UserDTO u = new UserDTO();
            u.setID_Engenheiro(user.getId());
            u.setNome_Engenheiro(user.getName());
            u.setEmail_Engenheiro(user.getUsername());
            u.setRole_Engenheiro(user.getRole().getRole());
            u.setStatus_Engenheiro(user.getStatus());
            return u;
        }).toList();

        return ResponseEntity.ok(dto);
    }
    
    
    @PostMapping("/de")
    public String deleteEngenheiro(@RequestParam String param) {
        User user = service.findById(Integer.parseInt(param));
        if (user != null && user.getStatus()) {
            user.setStatus(false); // Set status to false instead of deleting
            service.save(user);
        } else {
            return ResponseEntity.status(404).body("Engenheiro not found").toString();
        }

        return ResponseEntity.ok().body("Engenheiro deleted successfully").toString();
    }
}
