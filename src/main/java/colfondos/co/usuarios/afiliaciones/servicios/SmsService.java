package colfondos.co.usuarios.afiliaciones.servicios;

import colfondos.co.usuarios.afiliaciones.modelos.Afiliado;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger logger = LogManager.getLogger(SmsService.class);

    public void enviarNotificacion(Afiliado afiliado) {
        String mensaje = String.format(
                "Notificacion SMS - Afiliado actualizado: ID=%d, Nombre=%s, Email=%s, Celular=%s",
                afiliado.getId(), afiliado.getNombre(), afiliado.getEmail(), afiliado.getCelular());
        logger.info("Enviando SMS al celular {}: {}", afiliado.getCelular(), mensaje);
        logger.info("SMS enviado exitosamente a {}", afiliado.getCelular());
    }
}
