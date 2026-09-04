package colfondos.co.usuarios.afiliaciones.servicios;

import colfondos.co.usuarios.afiliaciones.modelos.Afiliado;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LogManager.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarNotificacion(Afiliado afiliado) {
        logger.info("Preparando email de notificacion para afiliado id: {} - email: {}", afiliado.getId(), afiliado.getEmail());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(afiliado.getEmail());
            helper.setSubject("Actualizacion de datos - Afiliado #" + afiliado.getId());

            String htmlContent = """
                    <html>
                    <body>
                        <h2>Notificacion de Actualizacion de Datos</h2>
                        <p>Estimado/a <strong>%s</strong>, sus datos han sido actualizados exitosamente.</p>
                        <h3>Datos actualizados:</h3>
                        <table border="1" cellpadding="8" cellspacing="0">
                            <tr><td><strong>Email</strong></td><td>%s</td></tr>
                            <tr><td><strong>Celular</strong></td><td>%s</td></tr>
                        </table>
                        <h3>Informacion del registro:</h3>
                        <table border="1" cellpadding="8" cellspacing="0">
                            <tr><td><strong>ID Afiliado</strong></td><td>%d</td></tr>
                            <tr><td><strong>Fecha Modificacion</strong></td><td>%s</td></tr>
                            <tr><td><strong>Usuario Modificacion</strong></td><td>%s</td></tr>
                        </table>
                        <br/>
                        <p>Si usted no solicito este cambio, por favor contacte al administrador.</p>
                        <p>Atentamente,<br/><strong>Colfondos</strong></p>
                    </body>
                    </html>
                    """.formatted(
                    afiliado.getNombre(),
                    afiliado.getEmail(),
                    afiliado.getCelular(),
                    afiliado.getId(),
                    afiliado.getFechaModificacion() != null ? afiliado.getFechaModificacion().toString() : "N/A",
                    afiliado.getUsuarioModificacion() != null ? afiliado.getUsuarioModificacion() : "N/A"
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);
            logger.info("Email enviado exitosamente a {}", afiliado.getEmail());
        } catch (MessagingException e) {
            logger.error("Error al enviar email a {}: {}", afiliado.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error al enviar email de notificacion", e);
        }
    }
}
