package puc.airtrack.airtrack.User;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserDTO;
import puc.airtrack.airtrack.Login.UserRole;
import puc.airtrack.airtrack.Login.UserService;
import puc.airtrack.airtrack.User.UserStatsDTO;

@Controller
@Tag(name = "👥 Usuários", description = "Gerenciamento completo de usuários do sistema - CRUD, estatísticas e controle de acesso por role")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private UserService service;

    @Operation(
        summary = "Criar novo usuário",
        description = "Cria um novo usuário no sistema com role específica (ADMIN, SUPERVISOR, ENGENHEIRO, AUDITOR). A senha é automaticamente criptografada.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do usuário a ser criado",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.class),
                examples = {
                    @ExampleObject(
                        name = "Engenheiro",
                        value = """
                        {
                          "name": "João Silva",
                          "username": "joao.silva@airtrack.com",
                          "password": "senha123",
                          "role": "ROLE_ENGENHEIRO",
                          "status": true,
                          "firstAccess": true,
                          "cpf": "123.456.789-00"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Supervisor",
                        value = """
                        {
                          "name": "Maria Santos",
                          "username": "maria.santos@airtrack.com",
                          "password": "senha456",
                          "role": "ROLE_SUPERVISOR",
                          "status": true,
                          "firstAccess": false,
                          "cpf": "987.654.321-00"
                        }
                        """
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuário criado com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "\"User created successfully\"")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Usuário já existe ou role inválida",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "\"User already exists\"")
            )
        ),
        @ApiResponse(responseCode = "401", ref = "UnauthorizedError"),
        @ApiResponse(responseCode = "403", ref = "ForbiddenError")
    })
    @PostMapping("/cre")
    public ResponseEntity<String> createUser(@RequestBody @Valid UserDTO entity) {
        if (service.findByUsername(entity.getUsername()) != null) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        if (entity.getRole() == null) {
            return ResponseEntity.badRequest().body("Invalid role");
        }
        String encodedPassword = new BCryptPasswordEncoder().encode(entity.getPassword());
        User user = new User();
        user.setName(entity.getName());
        user.setUsername(entity.getUsername());
        user.setPassword(encodedPassword);
        user.setStatus(entity.getStatus());
        user.setRole(entity.getRole());
        user.setFirstAccess(entity.getFirstAccess());
        user.setCpf(entity.getCpf());
        user.setId(service.newSave(user));
        URI location = URI.create("/user/get?id=" + user.getId() + "&role=" + user.getRole());
        return ResponseEntity.created(location).body("User created successfully");
    }

    @Operation(
        summary = "Atualizar usuário existente",
        description = "Atualiza os dados de um usuário existente. Apenas os campos fornecidos serão atualizados. A senha, se fornecida, será automaticamente criptografada.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados do usuário (ID é obrigatório)",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.class),
                examples = @ExampleObject(
                    name = "Atualizar senha",
                    value = """
                    {
                      "id": 1,
                      "name": "João Silva",
                      "username": "joao.silva@airtrack.com",
                      "password": "novaSenha456",
                      "role": "ROLE_ENGENHEIRO",
                      "status": true,
                      "firstAccess": false,
                      "cpf": "123.456.789-00"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário atualizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "\"User updated successfully\"")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Usuário não encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "\"User not found\"")
            )
        ),
        @ApiResponse(responseCode = "401", ref = "UnauthorizedError"),
        @ApiResponse(responseCode = "403", ref = "ForbiddenError")
    })
    @PutMapping("/upe")
    public ResponseEntity<String> updateUser(@RequestBody @Valid UserDTO entity) {
        User user = service.findById(entity.getId());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        if (entity.getName() != null && !entity.getName().isBlank()) {
            user.setName(entity.getName());
        }
        if (entity.getUsername() != null && !entity.getUsername().isBlank()) {
            user.setUsername(entity.getUsername());
        }
        String password = entity.getPassword();
        if (password != null && !password.isBlank()) {
            user.setPassword(new BCryptPasswordEncoder().encode(password));
        }
        if (entity.getRole() != null) {
            user.setRole(entity.getRole());
        }
        if (entity.getStatus() != null) {
            user.setStatus(entity.getStatus());
        }
        if (entity.getFirstAccess() != null) {
            user.setFirstAccess(entity.getFirstAccess());
        }
        if (entity.getCpf() != null && !entity.getCpf().isBlank()) {
            user.setCpf(entity.getCpf());
        }
        service.save(user);
        return ResponseEntity.ok("User updated successfully");
    }

    @Operation(
        summary = "Buscar usuário por ID e role",
        description = "Retorna os dados de um usuário específico identificado pelo ID e role. A combinação ID + role garante segurança adicional na busca."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "id": 1,
                      "name": "João Silva",
                      "username": "joao.silva@airtrack.com",
                      "role": "ROLE_ENGENHEIRO",
                      "status": true,
                      "firstAccess": false,
                      "cpf": "123.456.789-00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "404", ref = "NotFoundError"),
        @ApiResponse(responseCode = "401", ref = "UnauthorizedError")
    })
    @GetMapping("/ge")
    public ResponseEntity<UserDTO> getUser(
        @Parameter(description = "ID do usuário", example = "1", required = true)
        @RequestParam int id,
        @Parameter(description = "Role do usuário", example = "ROLE_ENGENHEIRO", required = true)
        @RequestParam UserRole role) {
        User user = service.findByIdAndRole(id, role);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setUsername(user.getUsername());
        userDTO.setRole(user.getRole());
        userDTO.setStatus(user.getStatus());
        userDTO.setFirstAccess(user.getFirstAccess());
        userDTO.setCpf(user.getCpf());
        return ResponseEntity.ok(userDTO);
    }

    @Operation(
        summary = "Listar usuários por role",
        description = "Retorna uma lista de todos os usuários com uma role específica (ADMIN, SUPERVISOR, ENGENHEIRO, AUDITOR)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuários retornada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.class)
            )
        ),
        @ApiResponse(responseCode = "401", ref = "UnauthorizedError"),
        @ApiResponse(responseCode = "403", ref = "ForbiddenError")
    })
    @GetMapping("/gel")
    public ResponseEntity<List<UserDTO>> getUserList(
        @Parameter(description = "Role dos usuários a listar", example = "ROLE_ENGENHEIRO", required = true)
        @RequestParam UserRole role) {
        List<User> userList = service.findAllByRole(role);
        List<UserDTO> dtoList = userList.stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setUsername(user.getUsername());
            dto.setRole(user.getRole());
            dto.setStatus(user.getStatus());
            dto.setFirstAccess(user.getFirstAccess());
            dto.setCpf(user.getCpf());
            return dto;
        }).toList();
        return ResponseEntity.ok(dtoList);
    }

    @Operation(
        summary = "Deletar usuário (soft delete)",
        description = "Desativa um usuário existente marcando seu status como false. O usuário não é removido do banco de dados, apenas inativado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário deletado com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "\"User deleted successfully\"")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado ou já inativo",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "\"User not found\"")
            )
        ),
        @ApiResponse(responseCode = "401", ref = "UnauthorizedError"),
        @ApiResponse(responseCode = "403", ref = "ForbiddenError")
    })
    @DeleteMapping("/de")
    public ResponseEntity<String> deleteUser(
        @Parameter(description = "ID do usuário a deletar", example = "1", required = true)
        @RequestParam int id) {
        User user = service.findById(id);
        if (user != null && Boolean.TRUE.equals(user.getStatus())) {
            user.setStatus(false); // Soft delete
            service.save(user);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    @Operation(
        summary = "Obter estatísticas de usuários (Admin)",
        description = "Retorna estatísticas consolidadas sobre usuários do sistema: total, ativos por role (Engenheiro, Auditor, Supervisor, Admin). Endpoint exclusivo para administradores."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Estatísticas retornadas com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserStatsDTO.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "totalUsuarios": 25,
                      "usuariosAtivos": 22,
                      "usuariosEngenheiro": 10,
                      "usuariosAuditor": 3,
                      "usuariosSupervisor": 5,
                      "usuariosAdmin": 4
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", ref = "UnauthorizedError"),
        @ApiResponse(responseCode = "403", ref = "ForbiddenError")
    })
    @GetMapping("/admin/users/stats")
    public ResponseEntity<UserStatsDTO> getUserStats() {
        List<User> todosUsuarios = service.findAll();
        
        long totalUsuarios = todosUsuarios.size();
        long usuariosAtivos = todosUsuarios.stream()
            .filter(u -> u.getStatus() != null && u.getStatus())
            .count();
        
        long usuariosEngenheiro = todosUsuarios.stream()
            .filter(u -> u.getRole() != null && u.getRole() == UserRole.ROLE_ENGENHEIRO && u.getStatus() != null && u.getStatus())
            .count();
        
        long usuariosAuditor = todosUsuarios.stream()
            .filter(u -> u.getRole() != null && u.getRole() == UserRole.ROLE_AUDITOR && u.getStatus() != null && u.getStatus())
            .count();
        
        long usuariosSupervisor = todosUsuarios.stream()
            .filter(u -> u.getRole() != null && u.getRole() == UserRole.ROLE_SUPERVISOR && u.getStatus() != null && u.getStatus())
            .count();
        
        long usuariosAdmin = todosUsuarios.stream()
            .filter(u -> u.getRole() != null && u.getRole() == UserRole.ROLE_ADMIN && u.getStatus() != null && u.getStatus())
            .count();
        
        UserStatsDTO stats = new UserStatsDTO();
        stats.setTotalUsuarios((int) totalUsuarios);
        stats.setUsuariosAtivos((int) usuariosAtivos);
        stats.setUsuariosEngenheiro((int) usuariosEngenheiro);
        stats.setUsuariosAuditor((int) usuariosAuditor);
        stats.setUsuariosSupervisor((int) usuariosSupervisor);
        stats.setUsuariosAdmin((int) usuariosAdmin);
        
        return ResponseEntity.ok(stats);
    }
}
