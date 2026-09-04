package colfondos.co.usuarios.afiliaciones.integracio;

import co.mycorp.security.spring_boot_security_jwt.models.ERole;
import co.mycorp.security.spring_boot_security_jwt.models.Role;
import co.mycorp.security.spring_boot_security_jwt.models.User;
import co.mycorp.security.spring_boot_security_jwt.respository.RoleRepository;
import co.mycorp.security.spring_boot_security_jwt.respository.UserRepository;
import colfondos.co.usuarios.afiliaciones.modelos.Afiliado;
import colfondos.co.usuarios.afiliaciones.repositorios.AfiliadoRepository;
import colfondos.co.usuarios.afiliaciones.servicios.EmailService;
import colfondos.co.usuarios.afiliaciones.servicios.SmsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AfiliadoRepository afiliadoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private SmsService smsService;

    private MvcResult signup(String username, String email, String password, Set<String> roles) throws Exception {
        return mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "email", email,
                                "password", password,
                                "role", roles == null ? Set.of() : roles))))
                .andReturn();
    }

    private String signin(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password))))
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return body.get("accessToken").asText();
    }

    private Afiliado seedAfiliado(String nombre, String email, String celular) {
        Afiliado afiliado = new Afiliado();
        afiliado.setNombre(nombre);
        afiliado.setEmail(email);
        afiliado.setCelular(celular);
        afiliado.setUsariomod("test-user");
        return afiliadoRepository.save(afiliado);
    }

    private void seedAdmin(String username, String password) {
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN no sembrado"));
        User admin = new User(username, username + "@test.com", passwordEncoder.encode(password));
        admin.setRoles(new HashSet<>(Set.of(adminRole)));
        userRepository.save(admin);
    }

    @Test
    void signup_signin_getAll_retorna200() throws Exception {
        signup("juanjwt", "juanjwt@test.com", "secret123", null);

        String token = signin("juanjwt", "secret123");

        mockMvc.perform(get("/api/afiliados").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAll_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/afiliados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_conUsuario_retorna403() throws Exception {
        signup("user1", "user1@test.com", "secret123", null);
        String token = signin("user1", "secret123");
        Afiliado afiliado = seedAfiliado("Juan Perez", "juan@test.com", "3001234567");

        mockMvc.perform(put("/api/afiliados/" + afiliado.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", afiliado.getNombre(),
                                "email", "nuevo@test.com",
                                "celular", "3009876543"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_conAdmin_retorna200() throws Exception {
        seedAdmin("admin1", "secret123");
        String token = signin("admin1", "secret123");
        Afiliado afiliado = seedAfiliado("Juan Perez", "juan@test.com", "3001234567");

        mockMvc.perform(put("/api/afiliados/" + afiliado.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", afiliado.getNombre(),
                                "email", "nuevo@test.com",
                                "celular", "3009876543"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nuevo@test.com"));
    }

    @Test
    void signup_ignoraRolesPrivilegiadosSolicitados() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "hacker1",
                                "email", "hacker1@test.com",
                                "password", "secret123",
                                "role", Set.of("admin")))))
                .andExpect(status().isOk());

        String token = signin("hacker1", "secret123");
        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "hacker1",
                                "password", "secret123"))))
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertEquals(1, body.get("roles").size());
        assertEquals("ROLE_USER", body.get("roles").get(0).asText());
        assertEquals(body.get("accessToken").asText(), token);
    }
}