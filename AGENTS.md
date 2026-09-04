# AGENTS.md

Spring Boot 4.1.1 (Java 17) REST API for "afiliados" with JWT auth provided by an external dependency on the `co.mycorp.security` library. No README; `pom.xml` and tests are the source of truth. All interaction is via the Maven wrapper (Windows PowerShell).

## Build & test
- Full suite: `.\mvnw.cmd test` (expect 32 tests, all passing).
- Single class: `.\mvnw.cmd test "-Dtest=JwtSecurityIntegrationTest"`
- Single method: `.\mvnw.cmd test "-Dtest=JwtSecurityIntegrationTest#update_conAdmin_retorna200"`
- Changing any config/scan wiring or the filter chain warrants a FULL suite run: slice, TLS, unit, and both integration contexts are sensitive to it.

## External JWT library (build prerequisite)
- `co.mycorp.security:spring-boot-security-jwt:0.0.1-SNAPSHOT` exists ONLY in the local `.m2`, built from `C:\localData\trainingws\springbootws\spring-boot-security-jwt`.
- If the build fails to resolve it (or after editing the library), reinstall from that directory: `.\mvnw.cmd install -DskipTests`.
- The library jar must stay THIN: its `spring-boot-maven-plugin` needs `<skip>true</skip>`, otherwise the installed "fat" jar hides classes and this app's component scanning finds nothing.

## Architecture wiring (not obvious from filenames)
`AfiliacionesApplication` is deliberately a bare `@SpringBootApplication`. Do NOT add `@ComponentScan`, `@EntityScan`, or `@EnableJpaRepositories` back onto it:
- An explicit top-level `@ComponentScan` replaces Boot's default scan and pulls nested test `@TestConfiguration` classes (e.g. `AfiliadoControllerTest.TestSecurityConfig`) into production contexts.
- `@EnableJpaRepositories`/`@EntityScan` on the primary source break `@WebMvcTest` slices (`No bean named 'entityManagerFactory'`).

The wiring lives in `config/`:
- `SecurityLibraryScanConfig.java`: `@ComponentScan("co.mycorp.security.spring_boot_security_jwt")` excluding `WebSecurityConfig`, `SpringBootSecurityJwtApplication`, `AuthController`, `TestController` — the library's own app/config controllers must never load here.
- `PersistenceConfig.java`: `@EntityScan` + `@EnableJpaRepositories` covering BOTH the app packages and the library packages (`...spring_boot_security_jwt.models`, `...respository`) — the lib's `UserRepository`/`RoleRepository` need them.
- `JwtSecurityConfig.java`: the only app-wide `SecurityFilterChain`; `@EnableMethodSecurity`; the `AuthTokenFilter` bean is also registered via a disabled `FilterRegistrationBean` to prevent double servlet registration.

Auth/roles are implemented by the LIBRARY, not this repo: entities `User`/`Role`/`ERole`, `UserRepository`/`RoleRepository` (package literally spelled `respository`), `JwtUtils`, `AuthEntryPointJwt`, `UserDetailsServiceImpl`, response `JwtResponse`. `AuthController` here only exposes signin/signup over those.

## Spring Boot 4 / Spring Security 7 quirks (verified in this repo)
- `DaoAuthenticationProvider` has no no-arg constructor — use `new DaoAuthenticationProvider(userDetailsService)`.
- Only ONE `SecurityFilterChain` may match an unqualified request. Any second any-request chain (e.g. a test config's chain bean) fails startup with `UnreachableFilterChainException`; scope test chains with `.securityMatcher(...)`.
- Boot 4 import moves in use: webmvc test annotations from `org.springframework.boot.webmvc.test.autoconfigure` (`@WebMvcTest`, `@AutoConfigureMockMvc`); `@MockitoBean` from `org.springframework.test.context.bean.override.mockito`.
- Logback is replaced by Log4j2 (`spring-boot-starter-log4j2`); log with `org.apache.logging.log4j.LogManager/Logger`.

## Domain & security model
- `Afiliado.id` is `Long` (wrapper) — KEEP it. A primitive `long` makes Jackson throw `Cannot map null into type long` when a PUT body omits `id`, which is the standard test pattern here.
- Signup is deliberately restricted: `AuthController.registerUser` always assigns only `ROLE_USER`; any requested admin/mod role is ignored (enforced by a test).
- Roles are seeded once via `src/main/resources/data.sql`; the `roles.name` column is an H2 ENUM limited to `ROLE_USER`/`ROLE_MODERATOR`/`ROLE_ADMIN`.
- Authz: `GET /api/afiliados**` requires USER/MODERATOR/ADMIN; `PUT /api/afiliados/{id}` requires ADMIN/MODERATOR. Enforced with `@PreAuthorize("hasAnyRole(...)")`.

## Tests
- `src/test/resources/application-test.properties` only disables TLS (`server.ssl.enabled=false`); the datasource comes from main `application.properties` (in-memory H2). TLS is ON by default otherwise (port 8443, `classpath:keystore.p12`, password via `KEYSTORE_PASS` env, default `changeit`).
- Two full-context integration classes, deliberately different auth styles:
  - `AfiliadoIntegrationTest` — `@WithMockUser(roles="ADMIN")` (skips the JWT flow).
  - `JwtSecurityIntegrationTest` — REAL signup → signin → Bearer token against the DB (this is where role enforcement is actually proven).
- Full-context (non-slice) tests use a plain `new ObjectMapper()` field, NOT `@Autowired` — there is no autowirable `ObjectMapper` in those contexts.
- `EmailService`/`SmsService` are `@MockitoBean` in integration tests.

## OpenSpec workflow
- The repo uses OpenSpec (`spec-driven` schema) with root in `openspec/`; artifacts are written in Spanish (config en `openspec/config.yaml`), keeping structural headings and SHALL/MUST keywords in English.
- Baseline specs exist under `openspec/specs/`: `auth`, `afiliados`, `transport`. New features MUST NOT be planned as direct spec edits — record them as a change (`/opsx-propose`, or `openspec new change <name>`) with `proposal.md`, delta `spec.md`, `design.md`, `tasks.md`; then implement and `/opsx-apply`, finally `/opsx-archive` to fold the delta into the main specs.
- Validate before/after changes: `openspec validate --all`. There are no open changes (`openspec/changes/` empty).