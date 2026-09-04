package colfondos.co.usuarios.afiliaciones.integracio;

import colfondos.co.usuarios.afiliaciones.modelos.Afiliado;
import colfondos.co.usuarios.afiliaciones.repositorios.AfiliadoRepository;
import colfondos.co.usuarios.afiliaciones.servicios.EmailService;
import colfondos.co.usuarios.afiliaciones.servicios.SmsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class AfiliadoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AfiliadoRepository afiliadoRepository;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private SmsService smsService;

    @BeforeEach
    void setUp() {
        afiliadoRepository.deleteAll();
        Afiliado afiliado = new Afiliado();
        afiliado.setNombre("Juan Perez");
        afiliado.setEmail("juan@test.com");
        afiliado.setCelular("3001234567");
        afiliado.setUsariomod("test-user");
        afiliadoRepository.save(afiliado);
    }

    @Test
    void getAll_retornaAfiliadosSembrados() throws Exception {
        mockMvc.perform(get("/api/afiliados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Juan Perez"))
                .andExpect(jsonPath("$[0].email").value("juan@test.com"));
    }

    @Test
    void getById_retornaAfiliadoSembrado() throws Exception {
        Afiliado afiliado = afiliadoRepository.findAll().get(0);

        mockMvc.perform(get("/api/afiliados/" + afiliado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(afiliado.getId()))
                .andExpect(jsonPath("$.nombre").value("Juan Perez"));
    }

    @Test
    void getById_retorna404CuandoNoExiste() throws Exception {
        mockMvc.perform(get("/api/afiliados/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Afiliado no encontrado con id: 99999"));
    }

    @Test
    void update_persisteEmailCelularYAuditoria() throws Exception {
        Afiliado afiliado = afiliadoRepository.findAll().get(0);

        Afiliado payload = new Afiliado();
        payload.setNombre(afiliado.getNombre());
        payload.setEmail("nuevo@test.com");
        payload.setCelular("3009876543");
        payload.setUsariomod("operador1");

        mockMvc.perform(put("/api/afiliados/" + afiliado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nuevo@test.com"))
                .andExpect(jsonPath("$.celular").value("3009876543"));

        mockMvc.perform(get("/api/afiliados/" + afiliado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nuevo@test.com"))
                .andExpect(jsonPath("$.usuarioModificacion").value("test-user"));
    }

    @Test
    void update_DatosInvalidos_noModificaRegistro() throws Exception {
        Afiliado afiliado = afiliadoRepository.findAll().get(0);

        Afiliado payload = new Afiliado();
        payload.setNombre(afiliado.getNombre());
        payload.setEmail("email-invalido");
        payload.setCelular("3001234567");

        mockMvc.perform(put("/api/afiliados/" + afiliado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.email").exists());

        mockMvc.perform(get("/api/afiliados/" + afiliado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }
}