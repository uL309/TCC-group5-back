package puc.airtrack.airtrack;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    private final AirtrackApplication airtrackApplication;

    SecurityConfig(AirtrackApplication airtrackApplication, SecurityFilter securityFilter) {
        this.airtrackApplication = airtrackApplication;
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        // Swagger / OpenAPI - Todos os endpoints possíveis
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/swagger-resources/**",
                                "/swagger-resources",
                                "/configuration/ui",
                                "/configuration/security",
                                "/webjars/**",
                                "/api-docs/**"
                        ).permitAll()

                        // Endpoints públicos - incluindo OPTIONS para preflight
                        .requestMatchers("/login", "/", "/register", "/reset-password").permitAll()
                        
                        // Health Check endpoints - públicos para Azure Container Apps probes
                        .requestMatchers("/health", "/ready", "/live").permitAll()
                        
                        // Spring Actuator health endpoints - públicos para Azure probes
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()

                        // ADMIN only - LOGS (deve vir ANTES de regras mais genéricas de /api/**)
                        .requestMatchers("/api/logs/**").hasRole("ADMIN")
                        
                        // Documentos - SUPERVISOR, ENGENHEIRO, AUDITOR ou ADMIN (deve vir ANTES de /api/**)
                        .requestMatchers("/api/documentos/**").hasAnyRole("SUPERVISOR", "ENGENHEIRO", "AUDITOR", "ADMIN")
                        
                        // Azure Blob Storage - endpoints para arquivos (deve vir ANTES de /api/**)
                        .requestMatchers("/api/files/upload").hasAnyRole("SUPERVISOR", "ENGENHEIRO", "ADMIN")
                        .requestMatchers("/api/files/list").hasAnyRole("SUPERVISOR", "ENGENHEIRO", "AUDITOR", "ADMIN")
                        .requestMatchers("/api/files/{fileName}").hasAnyRole("SUPERVISOR", "ENGENHEIRO", "AUDITOR", "ADMIN")
                        .requestMatchers("/api/files/**").hasAnyRole("SUPERVISOR", "ENGENHEIRO", "ADMIN")
                        
                        // Report API
                        .requestMatchers("/api/report/export").hasAnyRole("SUPERVISOR", "ADMIN")
                        
                        // ADMIN only - User management and role switching
                        .requestMatchers("/cre", "/ge", "/gel", "/upe", "/de", "/switch-role").hasRole("ADMIN")

                        // SUPERVISOR or ADMIN
                        .requestMatchers("/cforn", "/gforn", "/gforns", "/uforn", "/dforn",
                                "/linhaordem/get", "/linhaordem/list",
                                "/cmotor", "/umotor", "/gmotor","/gtipomotores", "/gtipomotorm").hasAnyRole("SUPERVISOR", "ADMIN")

                        // ENGENHEIRO or ADMIN
                        .requestMatchers("/cpeca", "/gpeca", "/gpecas", "/upeca", "/dpeca",
                                "/gfornc", "/linhaordem/**").hasAnyRole("ENGENHEIRO", "ADMIN")

                        // AUDITOR or ADMIN
                        .requestMatchers("/ccli", "/gcli", "/ucli", "/dcli").hasAnyRole("AUDITOR", "ADMIN")

                        // SUPERVISOR, ENGENHEIRO, or ADMIN
//                        .requestMatchers("/gmotores").hasAnyRole("SUPERVISOR", "ENGENHEIRO", "ADMIN")

                        // SUPERVISOR, AUDITOR, ENGENHEIRO, or ADMIN
                        .requestMatchers("/gclis","/gmotores").hasAnyRole("SUPERVISOR", "AUDITOR", "ENGENHEIRO", "ADMIN")

                        // ORDENS - controle específico por endpoint
                        .requestMatchers("/ordem/get", "/ordem/list").hasAnyRole("SUPERVISOR", "ENGENHEIRO", "AUDITOR", "ADMIN")
                        .requestMatchers("/ordem/update", "/ordem/atualizar-status").hasAnyRole("ENGENHEIRO", "SUPERVISOR", "ADMIN")
                        .requestMatchers("/ordem/create").hasAnyRole("SUPERVISOR", "ADMIN")
                        .requestMatchers("/ordem/{orderId}/anexos").hasAnyRole("SUPERVISOR", "ENGENHEIRO", "AUDITOR", "ADMIN")
                        .requestMatchers("/ordem/{orderId}/anexos/**").hasAnyRole("SUPERVISOR", "ENGENHEIRO", "AUDITOR", "ADMIN")
                        .requestMatchers("/ordem/{orderId}/pdf").hasAnyRole("SUPERVISOR", "ENGENHEIRO", "AUDITOR", "ADMIN")
                        
                        // Authenticated - endpoints que exigem apenas autenticação
                        .requestMatchers("/first-access", "/notifications/**").authenticated()
                )
                // CorsFilter PRIMEIRO - antes de qualquer outro filtro
                .addFilterBefore(corsFilter(), SecurityContextHolderFilter.class)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Esse bean usa a configuração global que criamos antes
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Sempre usa patterns para máxima flexibilidade
        // Permite localhost (dev) e todos os subdomínios do Azure Container Apps (prod)
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:4200",
            "http://localhost:*",
            "https://*.azurecontainerapps.io"
        ));
        
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Cache preflight por 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authconf) throws Exception {
        return authconf.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}