package colfondos.co.usuarios.afiliaciones.servicios;

import colfondos.co.usuarios.afiliaciones.modelos.Afiliado;
import colfondos.co.usuarios.afiliaciones.repositorios.AfiliadoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AfiliadoService {

    private static final Logger logger = LogManager.getLogger(AfiliadoService.class);

    private final AfiliadoRepository afiliadoRepository;
    private final SmsService smsService;
    private final EmailService emailService;

    public AfiliadoService(AfiliadoRepository afiliadoRepository, SmsService smsService, EmailService emailService) {
        this.afiliadoRepository = afiliadoRepository;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    public List<Afiliado> findAll() {
        logger.info("Consultando todos los afiliados");
        try {
            List<Afiliado> afiliados = afiliadoRepository.findAll();
            logger.info("Se encontraron {} afiliados", afiliados.size());
            return afiliados;
        } catch (Exception e) {
            logger.error("Error al consultar afiliados", e);
            throw e;
        }
    }

    public Afiliado findById(Long id) {
        logger.info("Buscando afiliado con id: {}", id);
        try {
            return afiliadoRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Afiliado no encontrado con id: {}", id);
                        return new RuntimeException("Afiliado no encontrado con id: " + id);
                    });
        } catch (RuntimeException e) {
            logger.error("Error al buscar afiliado con id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public Afiliado update(Long id, Afiliado afiliadoActualizado) {
        logger.info("Iniciando actualizacion de afiliado con id: {}", id);
        try {
            Afiliado existente = findById(id);
            existente.setEmail(afiliadoActualizado.getEmail());
            existente.setCelular(afiliadoActualizado.getCelular());
            actualizarDatosModificacion(existente);
            Afiliado guardado = afiliadoRepository.save(existente);
            logger.info("Afiliado con id: {} actualizado exitosamente", id);

            try {
                smsService.enviarNotificacion(guardado);
            } catch (Exception e) {
                logger.error("Error al enviar SMS de notificacion para afiliado id: {}", id, e);
            }

            try {
                emailService.enviarNotificacion(guardado);
            } catch (Exception e) {
                logger.error("Error al enviar email de notificacion para afiliado id: {}", id, e);
            }

            return guardado;
        } catch (Exception e) {
            logger.error("Error al actualizar afiliado con id: {}", id, e);
            throw e;
        }
    }

    private void actualizarDatosModificacion(Afiliado afiliado) {
        afiliado.setFechaModificacion(LocalDateTime.now());
        afiliado.setUsuarioModificacion(afiliado.getUsariomod());
    }
}
