package puc.airtrack.airtrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.interfaces.Claim;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import puc.airtrack.airtrack.Login.UserService;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    @Autowired
    TokenService tokenService;
    @Autowired
    UserService userService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);

        if (token != null) {
            var decodedJWT = tokenService.decodeToken(token);
            if (decodedJWT != null) {
                String username = decodedJWT.getSubject();
                String role = decodedJWT.getClaim("role").asString();
                if (username != null && role != null) {
                    UserDetails user = userService.findByUsername(username);
                    
                    // Cria lista de authorities
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(role));
                    
                    // Se o token tem originalRole e é ADMIN, adiciona ROLE_ADMIN às authorities
                    // Isso permite que o usuário continue usando endpoints de ADMIN mesmo após trocar de role
                    Claim originalRoleClaim = decodedJWT.getClaim("originalRole");
                    if (originalRoleClaim != null && !originalRoleClaim.isNull()) {
                        String originalRole = originalRoleClaim.asString();
                        if ("ROLE_ADMIN".equals(originalRole) && !role.equals("ROLE_ADMIN")) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        }
                    }
                    
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}

