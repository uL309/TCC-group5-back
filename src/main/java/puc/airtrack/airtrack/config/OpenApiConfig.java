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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Airtrack API",
        version = "1.0.0",
        description = """
            # 🛩️ Airtrack - Sistema de Gestão de Manutenção Aeronáutica
            
            API RESTful completa para gerenciamento de motores de aeronaves, ordens de serviço, peças e controle de manutenção.
            
            ## 📋 Funcionalidades Principais
            
            ### ✈️ Gestão de Motores
            - Cadastro completo de motores de aeronaves
            - Controle de TBO (Time Between Overhaul)
            - Histórico de manutenções
            - Alertas automáticos de vencimento
            
            ### 📝 Ordens de Serviço
            - Criação e gerenciamento de ordens de manutenção
            - Workflow de aprovações
            - Anexos e documentação técnica
            - Controle de status e prazos
            
            ### 🔧 Controle de Peças
            - Inventário completo de peças
            - Rastreabilidade por número de série
            - Controle de estoque
            - Gestão de fornecedores
            
            ### 👥 Gestão de Usuários
            - Controle de acesso por roles (ADMIN, SUPERVISOR, ENGENHEIRO, AUDITOR)
            - Autenticação JWT
            - Logs de auditoria completos
            
            ### 📊 Relatórios e Dashboards
            - Relatórios técnicos em PDF
            - Exportação de dados
            - Dashboards operacionais
            
            ### 📁 Gestão de Documentos
            - Upload e gerenciamento de manuais (MOM/MCQ)
            - Armazenamento seguro no Azure Blob Storage
            - Versionamento de documentos
            
            ### 🔔 Notificações
            - Alertas de TBO vencendo
            - Notificações de eventos críticos
            - Sistema de mensageria (RabbitMQ/Azure Storage Queue)
            
            ## 🔐 Autenticação
            
            A API utiliza **JWT (JSON Web Token)** para autenticação segura.
            
            ### Como autenticar:
            1. Faça login através do endpoint `POST /login`
            2. Copie o token JWT retornado no campo `token`
            3. Clique no botão **"Authorize"** 🔓 no topo desta página
            4. Insira: `Bearer {seu-token-jwt}`
            5. Clique em "Authorize" e depois "Close"
            
            ### Exemplo de token:
            ```
            Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
            ```
            
            ## 👤 Níveis de Acesso
            
            | Role | Permissões |
            |------|-----------|
            | **ADMIN** | Acesso total ao sistema, gestão de usuários |
            | **SUPERVISOR** | Gerenciamento de ordens de serviço, equipe e fornecedores |
            | **ENGENHEIRO** | Operações técnicas, relatórios e linhas de ordem |
            | **AUDITOR** | Acesso somente leitura para auditoria e relatórios |
            
            ## 📡 Endpoints Públicos
            
            Alguns endpoints **não** requerem autenticação:
            - `POST /login` - Autenticação de usuário
            - `POST /register` - Registro de novo usuário
            - `GET /health` - Health check do sistema
            - `GET /actuator/health` - Spring Actuator health
            
            ## ⚙️ Tecnologias
            
            - **Spring Boot 3.4.4** - Framework principal
            - **Spring Security** - Autenticação e autorização
            - **JWT** - Tokens de autenticação
            - **MySQL** - Banco de dados relacional
            - **Azure Blob Storage** - Armazenamento de arquivos
            - **RabbitMQ/Azure Storage Queue** - Mensageria
            - **Spring Actuator** - Monitoramento e health checks
            
            ## 🌐 Ambientes
            
            - **Desenvolvimento**: http://localhost:8080
            - **Produção**: https://ca-backend-airtrack-dev.kindhill-771aa15a.eastus.azurecontainerapps.io
            
            ## 📞 Suporte
            
            Em caso de dúvidas ou problemas, entre em contato:
            - **Email**: airtrack.pucpr@gmail.com
            - **Documentação**: https://airtrack.com/docs
            
            ---
            
            **Versão**: 1.0.0 | **Última atualização**: Novembro 2025
            """,
        contact = @Contact(
            name = "Equipe Airtrack - PUC-PR",
            email = "airtrack.pucpr@gmail.com",
            url = "https://github.com/airtrack"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            description = "🔧 Desenvolvimento Local",
            url = "http://localhost:8080"
        ),
        @Server(
            description = "☁️ Produção - Azure Container Apps",
            url = "https://ca-backend-airtrack-dev.kindhill-771aa15a.eastus.azurecontainerapps.io"
        )
    },
    security = {
        @SecurityRequirement(name = "bearerAuth")
    },
    tags = {
        @Tag(name = "🔐 Autenticação", description = "Endpoints de login, registro e autenticação JWT"),
        @Tag(name = "👥 Usuários", description = "Gestão de usuários e controle de acesso"),
        @Tag(name = "✈️ Motores", description = "Cadastro e gerenciamento de motores de aeronaves"),
        @Tag(name = "📝 Ordens de Serviço", description = "Criação e acompanhamento de ordens de manutenção"),
        @Tag(name = "🔧 Peças", description = "Inventário e controle de peças"),
        @Tag(name = "👔 Clientes", description = "Cadastro de proprietários de aeronaves"),
        @Tag(name = "🏭 Fornecedores", description = "Gerenciamento de fornecedores"),
        @Tag(name = "📁 Documentos", description = "Upload e gestão de manuais e documentos técnicos"),
        @Tag(name = "📊 Relatórios", description = "Geração de relatórios em PDF e exportação de dados"),
        @Tag(name = "📋 Logs", description = "Logs de auditoria e rastreabilidade"),
        @Tag(name = "🔔 Notificações", description = "Alertas e notificações do sistema"),
        @Tag(name = "❤️ Health Check", description = "Monitoramento e status do sistema")
    },
    externalDocs = @ExternalDocumentation(
        description = "📖 Documentação Completa do Airtrack",
        url = "https://github.com/airtrack/docs"
    )
)
@SecurityScheme(
    name = "bearerAuth",
    description = """
        ### 🔑 JWT Authentication
        
        Para autenticar suas requisições:
        1. Obtenha um token através do endpoint `/login`
        2. Clique no botão "Authorize" 🔓 acima
        3. Insira: `Bearer {seu-token}`
        4. Todas as requisições subsequentes usarão este token
        
        **Formato**: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
        
        O token é válido por 24 horas após o login.
        """,
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    
    @Value("${spring.application.name:Airtrack API}")
    private String applicationName;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    /**
     * Configuração customizada do OpenAPI com exemplos de respostas
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new io.swagger.v3.oas.models.info.Info()
                .title("🛩️ " + applicationName)
                .version("1.0.0")
                .description("API completa para gestão de manutenção aeronáutica")
                .contact(new io.swagger.v3.oas.models.info.Contact()
                    .name("Equipe Airtrack - PUC-PR")
                    .email("airtrack.pucpr@gmail.com")
                    .url("https://github.com/airtrack"))
                .license(new io.swagger.v3.oas.models.info.License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new io.swagger.v3.oas.models.servers.Server()
                    .url("http://localhost:" + serverPort)
                    .description("🔧 Desenvolvimento Local"),
                new io.swagger.v3.oas.models.servers.Server()
                    .url("https://ca-backend-airtrack-dev.kindhill-771aa15a.eastus.azurecontainerapps.io")
                    .description("☁️ Produção - Azure Container Apps")
            ))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new io.swagger.v3.oas.models.security.SecurityScheme()
                    .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT token obtido através do endpoint /login"))
                
                // Exemplos de respostas comuns
                .addResponses("UnauthorizedError", new ApiResponse()
                    .description("❌ Token JWT inválido ou ausente")
                    .content(new Content()
                        .addMediaType("application/json", new MediaType()
                            .example(Map.of(
                                "timestamp", "2025-11-10T10:30:00",
                                "status", 401,
                                "error", "Unauthorized",
                                "message", "Token JWT inválido ou expirado",
                                "path", "/api/endpoint"
                            )))))
                
                .addResponses("ForbiddenError", new ApiResponse()
                    .description("🚫 Acesso negado - Permissões insuficientes")
                    .content(new Content()
                        .addMediaType("application/json", new MediaType()
                            .example(Map.of(
                                "timestamp", "2025-11-10T10:30:00",
                                "status", 403,
                                "error", "Forbidden",
                                "message", "Você não tem permissão para acessar este recurso",
                                "path", "/api/endpoint"
                            )))))
                
                .addResponses("NotFoundError", new ApiResponse()
                    .description("🔍 Recurso não encontrado")
                    .content(new Content()
                        .addMediaType("application/json", new MediaType()
                            .example(Map.of(
                                "timestamp", "2025-11-10T10:30:00",
                                "status", 404,
                                "error", "Not Found",
                                "message", "Recurso solicitado não foi encontrado",
                                "path", "/api/endpoint"
                            )))))
                
                .addResponses("BadRequestError", new ApiResponse()
                    .description("⚠️ Requisição inválida")
                    .content(new Content()
                        .addMediaType("application/json", new MediaType()
                            .example(Map.of(
                                "timestamp", "2025-11-10T10:30:00",
                                "status", 400,
                                "error", "Bad Request",
                                "message", "Dados inválidos na requisição",
                                "errors", List.of(
                                    "Campo 'email' é obrigatório",
                                    "Campo 'senha' deve ter no mínimo 6 caracteres"
                                )
                            )))))
                
                .addResponses("ServerError", new ApiResponse()
                    .description("💥 Erro interno do servidor")
                    .content(new Content()
                        .addMediaType("application/json", new MediaType()
                            .example(Map.of(
                                "timestamp", "2025-11-10T10:30:00",
                                "status", 500,
                                "error", "Internal Server Error",
                                "message", "Ocorreu um erro inesperado no servidor"
                            )))))
            )
            .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerAuth"));
    }
}
