package colfondos.co.usuarios.afiliaciones.servicios;

import colfondos.co.usuarios.afiliaciones.modelos.Afiliado;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SmsServiceTest {

    private final SmsService smsService = new SmsService();

    private Afiliado crearAfiliado() {
        Afiliado afiliado = new Afiliado();
        afiliado.setId(1L);
        afiliado.setNombre("Juan Perez");
        afiliado.setEmail("juan@test.com");
        afiliado.setCelular("3001234567");
        return afiliado;
    }

    @Test
    void enviarNotificacion_NoLanzaExcepcion() {
        assertDoesNotThrow(() -> smsService.enviarNotificacion(crearAfiliado()));
    }
}