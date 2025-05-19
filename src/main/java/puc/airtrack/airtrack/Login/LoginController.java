package puc.airtrack.airtrack.Login;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestParam;
import puc.airtrack.airtrack.TokenService;
import puc.airtrack.airtrack.services.PasswordResetService;


@Controller
public class LoginController {

    @Autowired
    private AuthenticationManager AuthenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> postLogin(@RequestBody LoginDTO entity) {
        
        var UsernamePassword = new UsernamePasswordAuthenticationToken(entity.getUsername(), entity.getPassword());
        var auth = this.AuthenticationManager.authenticate(UsernamePassword);
        User user = (User) auth.getPrincipal();
        var token = tokenService.generateToken(user);
        return ResponseEntity.ok().body(new ResponseDTO(user.getName(), token));
    }


    // Endpoint para registrar um novo usuário(remover)
    @PostMapping("/register")
    public ResponseEntity<String> postRegister(@RequestBody UserDTO entity) {
        User user = new User();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setUsername(entity.getEmail_Engenheiro());
        user.setPassword(passwordEncoder.encode(entity.getSenha_Engenheiro()));
        user.setRole(entity.getRole_Engenheiro());
        user.setStatus(entity.getStatus_Engenheiro());
        user.setName(entity.getNome_Engenheiro());
        userService.save(user);
        return ResponseEntity.ok().body("User registered successfully: " + user.getUsername());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestParam String email) {
        passwordResetService.resetPassword(email);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/first-access")
    public ResponseEntity<String> updatePasswordOnFirstAccess(@RequestBody @Valid FirstAccessRequest request) {
        User user = userService.findByCpf(request.getCpf());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required");
        }

        user.setPassword(new BCryptPasswordEncoder().encode(request.getNewPassword()));
        user.setFirstAccess(Boolean.FALSE);
        userService.save(user);
        return ResponseEntity.ok("Password updated successfully");
    }
}
