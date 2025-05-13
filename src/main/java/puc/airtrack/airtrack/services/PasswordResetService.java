package puc.airtrack.airtrack.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import puc.airtrack.airtrack.Login.User;
import puc.airtrack.airtrack.Repositorio;

import java.security.SecureRandom;

@Service
public class PasswordResetService {

    private final Repositorio repo;
    private final EmailService emailService;

    public PasswordResetService(Repositorio repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    public void resetPassword(String email) {
        User user = repo.findByUsername(email);
        if (user == null) return;

        String novaSenha = generateRandomPassword(10);
        user.setPassword(new BCryptPasswordEncoder().encode(novaSenha));
        //TODO: Campo bool da primeira senha
        repo.save(user);

        try {
            emailService.enviarEmailComNovaSenha(
                    user.getName(),
                    user.getUsername(),
                    novaSenha,
                    "http://localhost:4200"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateRandomPassword(int length) {
        SecureRandom rnd = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
