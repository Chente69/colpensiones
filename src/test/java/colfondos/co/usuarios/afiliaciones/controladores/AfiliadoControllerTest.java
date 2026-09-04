package colfondos.co.usuarios.afiliaciones.controladores;

import colfondos.co.usuarios.afiliaciones.modelos.Afiliado;
import colfondos.co.usuarios.afiliaciones.servicios.AfiliadoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AfiliadoController.class)
@Import(AfiliadoControllerTest.TestSecurityConfig.class)
class AfiliadoControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                    .csrf(c -> c.disable())
                    .build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AfiliadoService afiliadoService;

    private Afiliado crearAfiliado(long id, String nombre, String email, String celular) {
        Afiliado afiliado = new Afiliado();
        afiliado.setId(id);
        afiliado.setNombre(nombre);
        afiliado.setEmail(email);
        afiliado.setCelular(celular);
        return afiliado;
    }

    @Test
    void getAll_retornaListaDeAfiliados() throws Exception {
        when(afiliadoService.findAll()).thenReturn(List.of(
                crearAfiliado(1, "Juan Perez", "juan@test.com", "3001234567"),
                crearAfiliado(2, "Maria Lopez", "maria@test.com", "3007654321")
        ));

        mockMvc.perform(get("/api/afiliados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Juan Perez"))
                .andExpect(jsonPath("$[1].email").value("maria@test.com"));
    }

    @Test
    void getById_retornaAfiliadoCuandoExiste() throws Exception {
        when(afiliadoService.findById(1L)).thenReturn(
                crearAfiliado(1, "Juan Perez", "juan@test.com", "3001234567"));

        mockMvc.perform(get("/api/afiliados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Perez"))
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    void getById_retorna404CuandoNoExiste() throws Exception {
        when(afiliadoService.findById(99L))
                .thenThrow(new RuntimeException("Afiliado no encontrado con id: 99"));

        mockMvc.perform(get("/api/afiliados/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Afiliado no encontrado con id: 99"));
    }

    @Test
    void update_actualizaAfiliadoCuandoDatosValidos() throws Exception {
        Afiliado actualizado = crearAfiliado(1, "Juan Perez", "nuevo@test.com", "3009876543");
        when(afiliadoService.update(eq(1L), any(Afiliado.class))).thenReturn(actualizado);

        String body = objectMapper.writeValueAsString(actualizado);

        mockMvc.perform(put("/api/afiliados/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nuevo@test.com"))
                .andExpect(jsonPath("$.celular").value("3009876543"));

        verify(afiliadoService).update(eq(1L), any(Afiliado.class));
    }

    @Test
    void update_retorna400CuandoEmailInvalido() throws Exception {
        Afiliado invalido = crearAfiliado(1, "Juan Perez", "email-invalido", "3001234567");
        String body = objectMapper.writeValueAsString(invalido);

        mockMvc.perform(put("/api/afiliados/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.email").exists());
    }

    @Test
    void update_retorna400CuandoCelularInvalido() throws Exception {
        Afiliado invalido = crearAfiliado(1, "Juan Perez", "juan@test.com", "123");
        String body = objectMapper.writeValueAsString(invalido);

        mockMvc.perform(put("/api/afiliados/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.celular").exists());
    }

    @Test
    void update_retorna400CuandoNombreVacio() throws Exception {
        Afiliado invalido = crearAfiliado(1, "", "juan@test.com", "3001234567");
        String body = objectMapper.writeValueAsString(invalido);

        mockMvc.perform(put("/api/afiliados/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());
    }

    @Test
    void update_retorna404CuandoAfiliadoNoExiste() throws Exception {
        Afiliado payload = crearAfiliado(1, "Juan Perez", "juan@test.com", "3001234567");
        String body = objectMapper.writeValueAsString(payload);

        when(afiliadoService.update(eq(1L), any(Afiliado.class)))
                .thenThrow(new RuntimeException("Afiliado no encontrado con id: 1"));

        mockMvc.perform(put("/api/afiliados/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Afiliado no encontrado con id: 1"));
    }
}