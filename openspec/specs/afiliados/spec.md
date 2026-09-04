## Purpose

Gestión de los afiliados: consulta de la lista y de un registro individual, actualización de email y celular con auditoría, y notificación al afiliado.

## Requirements

### Requirement: Consultar la lista completa de afiliados

La API SHALL exponer todos los afiliados registrados.

#### Scenario: Listado exitoso
- **WHEN** un usuario autenticado consulta `GET /api/afiliados`
- **THEN** la respuesta es 200 con un arreglo con todos los afiliados

### Requirement: Consultar un afiliado por su id

La API SHALL devolver los datos de un afiliado a partir de su identificador.

#### Scenario: Afiliado existente
- **WHEN** un usuario autenticado consulta `GET /api/afiliados/{id}` con el id de un afiliado existente
- **THEN** la respuesta es 200 con los datos del afiliado (id, nombre, email, celular y campos de auditoría)

#### Scenario: Afiliado inexistente
- **WHEN** un usuario autenticado consulta `GET /api/afiliados/{id}` con un id que no existe
- **THEN** la respuesta es 404 con el mensaje "Afiliado no encontrado con id: {id}"

### Requirement: Actualizar email y celular de un afiliado

La actualización SHALL cambiar solo el email y el celular del afiliado, conservar el id, el nombre y los campos de creación, y registrar la auditoría de la modificación. El campo `id` puede omitirse en el cuerpo de la petición.

#### Scenario: Actualización con datos válidos
- **WHEN** un usuario con rol ADMIN o MODERATOR envía `PUT /api/afiliados/{id}` con un email y celular válidos y sin incluir el campo `id`
- **THEN** la respuesta es 200 con el afiliado actualizado y el registro conserva su `fechaCreacion` mientras se registran `fechaModificacion` y `usuarioModificacion`

#### Scenario: Actualización con datos inválidos
- **WHEN** se envía `PUT /api/afiliados/{id}` con un email o celular no válidos
- **THEN** la respuesta es 400 que enumera los errores de campo (por ejemplo `errores.email`, `errores.celular`) y el registro no se modifica

### Requirement: Notificar al afiliado tras una actualización

Cuando un afiliado se actualiza correctamente, la aplicación SHALL disparar notificaciones por SMS y email al afiliado.

#### Scenario: Actualización exitosa
- **WHEN** la actualización de un afiliado se completa correctamente
- **THEN** se envían notificaciones por SMS y email al afiliado, y el fallo de una notificación no invalida la actualización