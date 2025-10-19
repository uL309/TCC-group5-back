package puc.airtrack.airtrack.Login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import puc.airtrack.airtrack.TokenService;
import puc.airtrack.airtrack.services.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Controller
@Tag(name = "Autenticação", description = "Endpoints de autenticação, registro e gerenciamento de senhas")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Operation(
        summary = "Realizar login",
        description = "Autentica um usuário e retorna um token JWT válido por 24 horas. Use este token no header Authorization para acessar endpoints protegidos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login realizado com sucesso - Token JWT retornado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseDTO.class),
                examples = @ExampleObject(
                    name = "Login bem-sucedido",
                    value = """
                        {
                          "name": "João Silva",
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJhaXJ0cmFjayIsInN1YiI6ImFkbWluQGFpcnRyYWNrLmNvbSIsImV4cCI6MTcwOTY3MDAwMH0.abc123xyz"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Unauthorized\""
                )
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> postLogin(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Credenciais de acesso",
            required = true,
            content = @Content(
                schema = @Schema(implementation = LoginDTO.class),
                examples = {
                    @ExampleObject(
                        name = "Admin",
                        value = """
                            {
                              "username": "admin@airtrack.com",
                              "password": "admin123"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Supervisor",
                        value = """
                            {
                              "username": "supervisor@airtrack.com",
                              "password": "super123"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Engenheiro",
                        value = """
                            {
                              "username": "engenheiro@airtrack.com",
                              "password": "eng123"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody LoginDTO entity) {
        
        var usernamePassword = new UsernamePasswordAuthenticationToken(entity.getUsername(), entity.getPassword());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        User user = (User) auth.getPrincipal();
        var token = tokenService.generateToken(user);
        return ResponseEntity.ok().body(new ResponseDTO(user.getName(), token));
    }


    // Endpoint para registrar um novo usuário (ajustado para novo padrão User/UserDTO)
    @PostMapping("/register")
    public ResponseEntity<String> postRegister(@RequestBody @Valid UserDTO entity) {
        User user = new User();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setUsername(entity.getUsername());
        user.setPassword(passwordEncoder.encode(entity.getPassword()));
        user.setRole(entity.getRole());
        user.setStatus(entity.getStatus());
        user.setName(entity.getName());
        user.setFirstAccess(entity.getFirstAccess());
        user.setCpf(entity.getCpf());
        userService.save(user);
        return ResponseEntity.ok().body("User registered successfully: " + user.getUsername());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestParam String username) {
        passwordResetService.resetPassword(username);
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
