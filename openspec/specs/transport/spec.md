## Purpose

Transporte HTTPS y cabeceras de seguridad aplicadas de forma global a las respuestas de la API de afiliados.

## Requirements

### Requirement: Exponer la API por HTTPS por defecto

La aplicación SHALL escuchar sobre HTTPS usando el certificado empaquetado en el classpath.

#### Scenario: Ejecución sin el perfil `test`
- **WHEN** la aplicación se inicia sin el perfil de Spring `test`
- **THEN** escucha en HTTPS sobre el puerto 8443 usando `classpath:keystore.p12` con la contraseña de la variable de entorno `KEYSTORE_PASS` (valor por defecto `changeit`)

#### Scenario: Ejecución con el perfil `test`
- **WHEN** la aplicación se inicia con el perfil de Spring `test`
- **THEN** TLS queda deshabilitado para permitir la ejecución de pruebas

### Requirement: Incluir cabeceras de seguridad en las respuestas

Todas las respuestas de la API SHALL incorporar cabeceras de seguridad de transporte.

#### Scenario: Respuesta de la API
- **WHEN** la aplicación responde una petición
- **THEN** la respuesta incluye HSTS con `preload`, la política de seguridad de contenido `default-src 'self'` y `DENY` para X-Frame-Options