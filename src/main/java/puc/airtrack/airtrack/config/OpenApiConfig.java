package puc.airtrack.airtrack.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Airtrack API",
        version = "1.0.0",
        description = """
            API para gerenciamento de motores de aeronaves, ordens de serviço, peças e controle de manutenção.
            
            ## Funcionalidades Principais:
            - **Gestão de Motores**: Cadastro, atualização e controle de motores de aeronaves
            - **Ordens de Serviço**: Criação e gerenciamento de ordens de manutenção
            - **Controle de Peças**: Inventário e movimentação de peças
            - **Gestão de Clientes**: Cadastro de proprietários de aeronaves
            - **Fornecedores**: Gerenciamento de fornecedores de peças e serviços
            - **Documentos**: Gestão de manuais (MOM/MCQ) no Azure Blob Storage
            - **Relatórios**: Geração de relatórios técnicos e operacionais
            - **Logs de Auditoria**: Rastreamento completo de todas as operações
            - **Notificações**: Sistema de alertas para TBO e eventos críticos
            
            ## Autenticação:
            A API utiliza JWT (JSON Web Token) para autenticação. Para acessar os endpoints protegidos:
            1. Faça login através de `/auth/login`
            2. Copie o token JWT retornado
            3. Clique em "Authorize" e insira: `Bearer {seu-token}`
            
            ## Níveis de Acesso:
            - **ADMIN**: Acesso total ao sistema
            - **SUPERVISOR**: Gerenciamento de ordens de serviço e equipe
            - **ENGENHEIRO**: Operações técnicas e relatórios
            - **AUDITOR**: Acesso somente leitura para auditoria
            """,
        contact = @Contact(
            name = "Equipe Airtrack",
            email = "contato@airtrack.com",
            url = "https://airtrack.com"
        ),
        license = @License(
            name = "Proprietary License",
            url = "https://airtrack.com/license"
        )
    ),
    servers = {
        @Server(
            description = "Servidor de Desenvolvimento",
            url = "http://localhost:8080"
        ),
        @Server(
            description = "Servidor de Produção",
            url = "https://api.airtrack.com"
        )
    },
    security = {
        @SecurityRequirement(name = "bearerAuth")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Authentication - Insira o token obtido no login (formato: Bearer {token})",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Configuração do OpenAPI/Swagger
}
