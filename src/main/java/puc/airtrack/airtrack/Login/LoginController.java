package puc.airtrack.airtrack.Login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import puc.airtrack.airtrack.TokenService;
import puc.airtrack.airtrack.services.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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


    @Operation(
        summary = "Registrar novo usuário",
        description = "Registra um novo usuário no sistema com senha criptografada. Este endpoint pode ser usado para auto-registro ou criação de usuários por administradores."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário registrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "\"User registered successfully: joao@airtrack.com\"")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos ou usuário já existe",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "\"Invalid user data\"")
            )
        )
    })
    // Endpoint para registrar um novo usuário (ajustado para novo padrão User/UserDTO)
    @PostMapping("/register")
    public ResponseEntity<String> postRegister(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do usuário a ser registrado",
            required = true,
            content = @Content(
                schema = @Schema(implementation = UserDTO.class),
                examples = @ExampleObject(
                    name = "Novo Engenheiro",
                    value = """
                    {
                      "name": "Carlos Souza",
                      "username": "carlos.souza@airtrack.com",
                      "password": "senha789",
                      "role": "ROLE_ENGENHEIRO",
                      "status": true,
                      "firstAccess": true,
                      "cpf": "456.789.123-00"
                    }
                    """
                )
            )
        )
        @RequestBody @Valid UserDTO entity) {
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

    @Operation(
        summary = "Resetar senha do usuário",
        description = "Inicia o processo de reset de senha para um usuário. Um e-mail com instruções será enviado para o usuário."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Processo de reset iniciado com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado"
        )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
        @Parameter(description = "Username/email do usuário", example = "joao@airtrack.com", required = true)
        @RequestParam String username) {
        passwordResetService.resetPassword(username);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Atualizar senha no primeiro acesso",
        description = "Permite que um usuário altere sua senha no primeiro acesso ao sistema. O usuário é identificado pelo CPF e a flag firstAccess é automaticamente marcada como false."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Senha atualizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "\"Password updated successfully\"")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "\"User not found\"")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Senha inválida ou não fornecida",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "\"Password is required\"")
            )
        )
    })
    @PutMapping("/first-access")
    public ResponseEntity<String> updatePasswordOnFirstAccess(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "CPF e nova senha",
            required = true,
            content = @Content(
                schema = @Schema(implementation = FirstAccessRequest.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "cpf": "123.456.789-00",
                      "newPassword": "minhaNovaSenha123"
                    }
                    """
                )
            )
        )
        @RequestBody @Valid FirstAccessRequest request) {
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

    @Operation(
        summary = "Trocar role (apenas ADMIN)",
        description = "Permite que um ADMIN gere um novo token com uma role diferente para visualizar a interface como outra role. Se targetRole for null, retorna o token original do ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token gerado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Apenas ADMIN pode usar este endpoint",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/switch-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> switchRole(@RequestParam(required = false) String targetRole) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Se targetRole for null ou ADMIN, retorna token original
        if (targetRole == null || targetRole.equals("ROLE_ADMIN")) {
            var token = tokenService.generateToken(user);
            return ResponseEntity.ok().body(new ResponseDTO(user.getName(), token));
        }

        // Valida se a role target é válida
        UserRole overrideRole;
        try {
            overrideRole = UserRole.valueOf(targetRole);
            // Não permite usar ADMIN como override
            if (overrideRole == UserRole.ROLE_ADMIN) {
                var token = tokenService.generateToken(user);
                return ResponseEntity.ok().body(new ResponseDTO(user.getName(), token));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        // Gera token com role override
        var token = tokenService.generateTokenWithRoleOverride(user, overrideRole);
        return ResponseEntity.ok().body(new ResponseDTO(user.getName(), token));
    }
}
