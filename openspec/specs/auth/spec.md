## Purpose

Autenticación por JWT y autorización por roles para la API de afiliados, basadas en la librería `co.mycorp.security`.

## Requirements

### Requirement: El registro de usuarios asigna siempre el rol ROLE_USER

El registro SHALL asignar únicamente el rol `ROLE_USER` al usuario, ignorando cualquier rol privilegiado solicitado.

#### Scenario: Registro solicitando un rol privilegiado
- **WHEN** un cliente envía `POST /api/auth/signup` solicitando el rol `admin` o `moderator`
- **THEN** el usuario queda registrado con un único rol `ROLE_USER`

#### Scenario: Registro con username o email ya utilizados
- **WHEN** un cliente envía `POST /api/auth/signup` con un username o email ya registrado
- **THEN** la respuesta es 400 indicando que el nombre de usuario o el email ya están en uso

### Requirement: El inicio de sesión devuelve un token JWT y los roles del usuario

El inicio de sesión SHALL devolver un token Bearer y los roles del usuario cuando este se autentica con credenciales correctas.

#### Scenario: Credenciales válidas
- **WHEN** un usuario registrado envía `POST /api/auth/signin` con username y contraseña correctos
- **THEN** la respuesta es 200 e incluye `accessToken`, el identificador, el username, el email y la lista de roles

#### Scenario: Credenciales inválidas
- **WHEN** un cliente envía `POST /api/auth/signin` con credenciales incorrectas
- **THEN** la respuesta es 401 y no se emite ningún token

### Requirement: Las consultas de afiliados exigen autenticación y rol USER, MODERATOR o ADMIN

Toda consulta a los afiliados SHALL requerir un token Bearer válido con uno de los roles `ROLE_USER`, `ROLE_MODERATOR` o `ROLE_ADMIN`.

#### Scenario: Solicitud sin token
- **WHEN** un cliente consulta `GET /api/afiliados` sin token Bearer
- **THEN** la respuesta es 401 Unauthorized

#### Scenario: Usuario autenticado con ROLE_USER
- **WHEN** un usuario con rol `ROLE_USER` y token válido consulta `GET /api/afiliados` o `GET /api/afiliados/{id}`
- **THEN** la respuesta es 200

### Requirement: La actualización de afiliados exige rol ADMIN o MODERATOR

La actualización de un afiliado SHALL requerir un token Bearer válido con rol `ROLE_ADMIN` o `ROLE_MODERATOR`.

#### Scenario: Usuario con ROLE_USER
- **WHEN** un usuario con rol `ROLE_USER` envía `PUT /api/afiliados/{id}`
- **THEN** la respuesta es 403 Forbidden

#### Scenario: Usuario con ROLE_ADMIN
- **WHEN** un usuario con rol `ROLE_ADMIN` envía `PUT /api/afiliados/{id}` con datos válidos
- **THEN** la respuesta es 200 y el afiliado queda actualizado