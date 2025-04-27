package puc.airtrack.airtrack.Login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import puc.airtrack.airtrack.TokenService;


@Controller
public class LoginController {

    @Autowired
    private AuthenticationManager AuthenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> postLogin(@RequestBody LoginDTO entity) {
        
        var UsernamePassword = new UsernamePasswordAuthenticationToken(entity.getUsername(), entity.getPassword());
        var auth = this.AuthenticationManager.authenticate(UsernamePassword);
        User user = (User) auth.getPrincipal();
        var token = tokenService.generateToken(user);
        return ResponseEntity.ok().body(new ResponseDTO(user.getName(), token));
    }

    @PostMapping("/register")
    public ResponseEntity<String> postRegister(@RequestBody UserDTO entity) {
        User user = new User();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setUsername(entity.getEmail_Engenheiro());
        user.setPassword(passwordEncoder.encode(entity.getSenha_Engenheiro()));
        user.setRole(UserRole.fromRoleValue(entity.getRole_Engenheiro()));
        user.setStatus(entity.getStatus_Engenheiro());
        user.setName(entity.getNome_Engenheiro());
        userService.save(user);
        return ResponseEntity.ok().body("User registered successfully: " + user.getUsername());
    }
}
