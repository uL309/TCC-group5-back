package puc.airtrack.airtrack.services;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Base64;
import java.util.Objects;
import org.springframework.util.StreamUtils;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private String toDataUri(String classpathResourcePath) throws IOException {
        ClassPathResource res = new ClassPathResource(classpathResourcePath);
        byte[] bytes = StreamUtils.copyToByteArray(res.getInputStream());
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String ext = classpathResourcePath.substring(classpathResourcePath.lastIndexOf('.') + 1);
        return "data:image/" + ext + ";base64," + base64;
    }

    public void enviarEmailComNovaSenha(
            String nome,
            String email,
            String novaSenha,
            String resetUrl
    ) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setTo(email);
        helper.setFrom("airtrack.pucpr@gmail.com", "AirTrack");
        helper.setSubject("Nova senha de acesso - AirTrack");

        // carregar template
        String template = new String(
                Objects.requireNonNull(getClass()
                                .getResourceAsStream("/templates/password-reset.html"))
                        .readAllBytes(),
                StandardCharsets.UTF_8
        );

        // gerar Data URIs
        String logoLightUri = toDataUri("static/logo_light.webp");
        String logoDarkUri  = toDataUri("static/logo_dark.webp");

        // substituir placeholders
        String html = template
                .replace("{{LOGO_LIGHT_URI}}", logoLightUri)
                .replace("{{LOGO_DARK_URI}}", logoDarkUri)
                .replace("{{NAME}}", nome)
                .replace("{{EMAIL}}", email)
                .replace("{{PASSWORD}}", novaSenha)
                .replace("{{URL_BASE}}", resetUrl)
                .replace("{{CURRENT_YEAR}}", String.valueOf(Year.now().getValue()));

        helper.setText(html, true);
        mailSender.send(message);
    }
}
