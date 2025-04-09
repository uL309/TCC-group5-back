package puc.airtrack.airtrack.Login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class LoginController {
    @Autowired
    private UserService userService;
    String jwt;
    
    @PostMapping("/login")
    public ResponseEntity postLogin(@RequestBody UserDTO entity) {
        
        User data=userService.findByUsernameAndPassword(entity.getUsername(), entity.getPassword());
            if(entity.getUsername() == data.getUsername() && entity.getPassword() == data.getPassword() && data.getRole() == 4) {
                /*meter o jwt aqui*/
                return ResponseEntity.ok("admin");
            } else if(entity.getUsername() == data.getUsername() && entity.getPassword() == data.getPassword() && data.getRole() == 1) {
                /*meter o jwt aqui*/
                return ResponseEntity.ok("user");
            }
        
        return ResponseEntity.status(401).body("Invalid username or password");
    }
    
}
