package puc.airtrack.airtrack.Engenheiro;


import java.net.URI;
import java.util.ArrayList;

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
    private UserService repositorio;
    
    @PostMapping("/cre")
    public ResponseEntity<String> createEngenheiro(@RequestBody @Valid UserDTO entity) {
        if (this.repositorio.findByUsername(entity.getEmail_Engenheiro()) != null) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        String epassword = new BCryptPasswordEncoder().encode(entity.getSenha_Engenheiro());
        User user = new User();
        user.setName(entity.getNome_Engenheiro());
        user.setUsername(entity.getEmail_Engenheiro());
        user.setPassword(epassword);
        user.setRole(UserRole.fromRoleValue(entity.getRole_Engenheiro()));
        user.setStatus(entity.getStatus_Engenheiro());
        repositorio.save(user);
        
        URI location = URI.create("/ge/" + user.getId());
        return ResponseEntity.created(location).body("Engenheiro created successfully");
    }
    
    @PutMapping("/upe")
    public String UpdateEngenheiro(@RequestBody @Valid UserDTO entity) {
        String epassword = new BCryptPasswordEncoder().encode(entity.getSenha_Engenheiro());
        if (getEngenheiro(entity.getID_Engenheiro().toString()) == null) {
            return ResponseEntity.badRequest().body("User not found").toString();
        }
        User user = repositorio.findById(entity.getID_Engenheiro());
        user.setName(entity.getNome_Engenheiro());
        user.setUsername(entity.getEmail_Engenheiro());
        user.setPassword(epassword);
        user.setRole(UserRole.fromRoleValue(entity.getRole_Engenheiro()));
        user.setStatus(entity.getStatus_Engenheiro());
        repositorio.save(user);

        return ResponseEntity.ok().body("Engenheiro updated successfully").toString();
    }

    @GetMapping("/ge")
    public ResponseEntity<UserDTO> getEngenheiro(@RequestParam String param) {
        User user = repositorio.findById(Integer.parseInt(param));
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
    public ResponseEntity<ArrayList<UserDTO>> getEngenheirolist() {
        ArrayList<UserDTO> userDTOList = new ArrayList<>();
        ArrayList<User> userList = (ArrayList<User>) repositorio.findAll();
        for (User user : userList) {
            UserDTO userDTO = new UserDTO();
            userDTO.setNome_Engenheiro(user.getName());
            userDTO.setID_Engenheiro(user.getId()); 
            userDTO.setEmail_Engenheiro(user.getUsername());
            userDTO.setSenha_Engenheiro(user.getPassword());
            userDTO.setRole_Engenheiro(user.getRole().ordinal());
            userDTO.setStatus_Engenheiro(user.getStatus());
    
            userDTOList.add(userDTO);
        }
        return ResponseEntity.ok().body(userDTOList);
    }
    
    
    @PostMapping("/de")
    public String deleteEngenheiro(@RequestParam String param) {
        User user = repositorio.findById(Integer.parseInt(param));
        if (user != null && user.getStatus()) {
            user.setStatus(false); // Set status to false instead of deleting
            repositorio.save(user);
        } else {
            return ResponseEntity.status(404).body("Engenheiro not found").toString();
        }

        return ResponseEntity.ok().body("Engenheiro deleted successfully").toString();
    }
}
