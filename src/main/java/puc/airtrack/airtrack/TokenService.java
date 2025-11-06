package puc.airtrack.airtrack;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Login.UserRole;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(User user){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            String token = JWT.create()
                    .withIssuer("login-auth-Backend")
                    .withSubject(user.getUsername())
                    .withClaim("role", user.getRole().toString())
                    .withClaim("originalRole", user.getRole().toString()) // Role original do usuário
                    .withClaim("firstAccess", user.getFirstAccess())
                    .withClaim("cpf", user.getCpf())
                    .withClaim("id", user.getId())
                    .withExpiresAt(this.generateExpirationDate())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception){
            throw new RuntimeException("Error while authenticating");
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("login-auth-Backend")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public DecodedJWT decodeToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("login-auth-Backend")
                    .build()
                    .verify(token);
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    /**
     * Gera um token com role override (apenas para ADMIN)
     * @param user Usuário original
     * @param overrideRole Role para usar no token (deve ser diferente de ADMIN)
     * @return Token JWT com role override, mantendo originalRole como ADMIN
     */
    public String generateTokenWithRoleOverride(User user, UserRole overrideRole) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            String token = JWT.create()
                    .withIssuer("login-auth-Backend")
                    .withSubject(user.getUsername())
                    .withClaim("role", overrideRole.toString()) // Role atual (override)
                    .withClaim("originalRole", user.getRole().toString()) // Mantém a role original (ADMIN)
                    .withClaim("firstAccess", user.getFirstAccess())
                    .withClaim("cpf", user.getCpf())
                    .withClaim("id", user.getId())
                    .withExpiresAt(this.generateExpirationDate())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception){
            throw new RuntimeException("Error while generating token with role override");
        }
    }

    private Date generateExpirationDate(){
        return Date.from(LocalDateTime.now().plusHours(12).toInstant(ZoneOffset.of("-03:00")));
    }
}
