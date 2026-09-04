package colfondos.co.usuarios.afiliaciones.servicios;

import colfondos.co.usuarios.afiliaciones.modelos.Afiliado;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "from@colfondos.com");
    }

    private Afiliado crearAfiliado() {
        Afiliado afiliado = new Afiliado();
        afiliado.setId(1L);
        afiliado.setNombre("Juan Perez");
        afiliado.setEmail("juan@test.com");
        afiliado.setCelular("3001234567");
        afiliado.setUsariomod("admin123");
        return afiliado;
    }

    @Test
    void enviarNotificacion_enviaEmailCuandoDatosValidos() throws Exception {
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.enviarNotificacion(crearAfiliado());

        verify(mailSender).send(any(MimeMessage.class));
        assertTrue(message.getSubject().contains("Afiliado #1"));
        assertTrue(message.getAllRecipients().length == 1);
        assertTrue(message.getAllRecipients()[0].toString().contains("juan@test.com"));
    }

    @Test
    void enviarNotificacion_lanzaRuntimeExceptionCuandoFallaelEnvio() throws Exception {
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () -> emailService.enviarNotificacion(crearAfiliado()));
    }
}