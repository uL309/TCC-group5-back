package puc.airtrack.airtrack.Engenheiro;


import java.util.ArrayList;

import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import puc.airtrack.airtrack.Repositorio;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserDTO;
import puc.airtrack.airtrack.Login.UserService;



@Controller
public class EngenheiroController {

    @Autowired
    private UserService repositorio;
    
    @PostMapping("/cre")
    public String CreateEngenheiro(@RequestBody UserDTO entity) {
        User user = new User();
        user.setName(entity.getName());
        user.setUsername(entity.getUsername());
        user.setPassword(entity.getPassword());
        user.setRole(entity.getRole());
        user.setStatus(entity.getStatus());
        repositorio.save(user);
        
        return ResponseEntity.ok().body("Engenheiro created successfully").toString();
    }
    
    @PostMapping("/upe")
    public String UpdateEngenheiro(@RequestBody UserDTO entity) {
        User user = new User();
        user.setName(entity.getName());
        user.setUsername(entity.getUsername());
        user.setPassword(entity.getPassword());
        user.setRole(entity.getRole());
        user.setStatus(entity.getStatus());
        repositorio.save(user);

        return ResponseEntity.ok().body("Engenheiro updated successfully").toString();
    }

    @GetMapping("/ge")
    public ResponseEntity<UserDTO> getEngenheiro(@RequestParam String param) {
        User user = repositorio.findById(Integer.parseInt(param));
        UserDTO userDTO = new UserDTO();
        userDTO.setName(user.getName());
        userDTO.setId(user.getId()); 
        userDTO.setUsername(user.getUsername());
        userDTO.setPassword(user.getPassword());
        userDTO.setRole(user.getRole());
        userDTO.setStatus(user.getStatus());

        return ResponseEntity.ok().body(userDTO);
    }

    @GetMapping("/gel")
    public ResponseEntity<ArrayList<UserDTO>> getEngenheirolist(@RequestParam String param) {
        ArrayList<UserDTO> userDTOList = new ArrayList<>();
        ArrayList<User> userList = (ArrayList<User>) repositorio.findAll();
        for (User user : userList) {
            UserDTO userDTO = new UserDTO();
            userDTO.setName(user.getName());
            userDTO.setId(user.getId()); 
            userDTO.setUsername(user.getUsername());
            userDTO.setPassword(user.getPassword());
            userDTO.setRole(user.getRole());
            userDTO.setStatus(user.getStatus());
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
