package puc.airtrack.airtrack.Login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class LoginController {
    String[] dados;
    String jwt;
    @PostMapping("/login")
    public String postMethodName(@RequestBody UserDTO entity) {
        /* colocar a chamada do banco de dados, lembrar de fazer dinamicamente e não armazenar! */
        
        if(entity.getUsername().equals("admin") && entity.getPassword().equals("admin")) {
            return "admin";
        } else if(entity.getUsername() == dados[0] && entity.getPassword() == dados[1]) {
            return "user";
        }
        
        return jwt;
    }
    
}
