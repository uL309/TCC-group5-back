package puc.airtrack.airtrack;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
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
                        // Swagger / OpenAPI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Endpoints públicos
                        .requestMatchers("/login", "/", "/register", "/reset-password").permitAll()

                        // ADMIN only
                        .requestMatchers("/cre", "/ge", "/gel", "/upe", "/de").hasRole("ADMIN")

                        // SUPERVISOR or ADMIN
                        .requestMatchers("/cforn", "/gforn", "/gforns", "/uforn", "/dforn",
                                "/linhaordem/get", "/linhaordem/list",
                                "/cmotor", "/umotor", "/gmotor").hasAnyRole("SUPERVISOR", "ADMIN")

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

                        // Authenticated
                        .requestMatchers("/first-access").authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Esse bean usa a configuração global que criamos antes
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
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