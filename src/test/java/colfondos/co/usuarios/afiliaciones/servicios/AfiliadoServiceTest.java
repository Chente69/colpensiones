package colfondos.co.usuarios.afiliaciones.servicios;

import colfondos.co.usuarios.afiliaciones.modelos.Afiliado;
import colfondos.co.usuarios.afiliaciones.repositorios.AfiliadoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AfiliadoServiceTest {

    @Mock
    private AfiliadoRepository afiliadoRepository;

    @Mock
    private SmsService smsService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AfiliadoService afiliadoService;

    private Afiliado crearAfiliado(long id, String nombre, String email, String celular, String usariomod) {
        Afiliado afiliado = new Afiliado();
        afiliado.setId(id);
        afiliado.setNombre(nombre);
        afiliado.setEmail(email);
        afiliado.setCelular(celular);
        afiliado.setUsariomod(usariomod);
        return afiliado;
    }

    @Test
    void findAll_retornaTodosLosAfiliados() {
        List<Afiliado> afiliados = List.of(
                crearAfiliado(1, "Juan Perez", "juan@test.com", "3001234567", "admin"),
                crearAfiliado(2, "Maria Lopez", "maria@test.com", "3007654321", "admin")
        );
        when(afiliadoRepository.findAll()).thenReturn(afiliados);

        List<Afiliado> resultado = afiliadoService.findAll();

        assertEquals(2, resultado.size());
        verify(afiliadoRepository).findAll();
    }

    @Test
    void findById_retornaAfiliadoCuandoExiste() {
        Afiliado afiliado = crearAfiliado(1, "Juan Perez", "juan@test.com", "3001234567", "admin");
        when(afiliadoRepository.findById(1L)).thenReturn(Optional.of(afiliado));

        Afiliado resultado = afiliadoService.findById(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Juan Perez", resultado.getNombre());
    }

    @Test
    void findById_lanzaExcepcionCuandoNoExiste() {
        when(afiliadoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> afiliadoService.findById(99L));

        assertEquals("Afiliado no encontrado con id: 99", ex.getMessage());
    }

    @Test
    void update_actualizaCamposYAuditoria() {
        Afiliado existente = crearAfiliado(1, "Juan Perez", "juan@test.com", "3001234567", "admin123");
        Afiliado actualizado = crearAfiliado(1, "Juan Perez", "nuevo@test.com", "3009876543", "admin123");

        when(afiliadoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(afiliadoRepository.save(any(Afiliado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Afiliado resultado = afiliadoService.update(1L, actualizado);

        assertEquals("nuevo@test.com", resultado.getEmail());
        assertEquals("3009876543", resultado.getCelular());
        assertNotNull(resultado.getFechaModificacion());
        assertEquals("admin123", resultado.getUsuarioModificacion());
        verify(afiliadoRepository).save(existente);
        verify(smsService).enviarNotificacion(existente);
        verify(emailService).enviarNotificacion(existente);
    }

    @Test
    void update_cuandoSmsFalla_aunActualizaYEnviaEmail() {
        Afiliado existente = crearAfiliado(1, "Juan Perez", "juan@test.com", "3001234567", "admin123");
        Afiliado actualizado = crearAfiliado(1, "Juan Perez", "nuevo@test.com", "3009876543", "admin123");

        when(afiliadoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(afiliadoRepository.save(any(Afiliado.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Fallo SMS")).when(smsService).enviarNotificacion(any(Afiliado.class));

        Afiliado resultado = afiliadoService.update(1L, actualizado);

        assertEquals("nuevo@test.com", resultado.getEmail());
        verify(emailService).enviarNotificacion(existente);
    }

    @Test
    void update_cuandoEmailFalla_aunActualiza() {
        Afiliado existente = crearAfiliado(1, "Juan Perez", "juan@test.com", "3001234567", "admin123");
        Afiliado actualizado = crearAfiliado(1, "Juan Perez", "nuevo@test.com", "3009876543", "admin123");

        when(afiliadoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(afiliadoRepository.save(any(Afiliado.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Fallo Email")).when(emailService).enviarNotificacion(any(Afiliado.class));

        Afiliado resultado = afiliadoService.update(1L, actualizado);

        assertEquals("nuevo@test.com", resultado.getEmail());
        verify(smsService).enviarNotificacion(existente);
    }

    @Test
    void update_lanzaExcepcionCuandoAfiliadoNoExiste() {
        Afiliado actualizado = crearAfiliado(1, "Juan Perez", "nuevo@test.com", "3009876543", "admin123");
        when(afiliadoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> afiliadoService.update(1L, actualizado));

        verify(afiliadoRepository, never()).save(any(Afiliado.class));
    }

    @Test
    void update_persisteFechaModificacionEnLaMismaFecha() {
        Afiliado existente = crearAfiliado(1, "Juan Perez", "juan@test.com", "3001234567", "admin123");
        Afiliado actualizado = crearAfiliado(1, "Juan Perez", "nuevo@test.com", "3009876543", "admin123");

        when(afiliadoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(afiliadoRepository.save(any(Afiliado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime antes = LocalDateTime.now();
        afiliadoService.update(1L, actualizado);
        LocalDateTime despues = LocalDateTime.now();

        assertNotNull(existente.getFechaModificacion());
        assertEquals(existente.getFechaModificacion().getYear(), antes.getYear());
        assertEquals(existente.getFechaModificacion().getYear(), despues.getYear());
    }
}