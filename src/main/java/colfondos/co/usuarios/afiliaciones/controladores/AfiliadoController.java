package colfondos.co.usuarios.afiliaciones.controladores;

import colfondos.co.usuarios.afiliaciones.modelos.Afiliado;
import colfondos.co.usuarios.afiliaciones.servicios.AfiliadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/afiliados")
@Tag(name = "Afiliados", description = "API para gestionar afiliados")
public class AfiliadoController {

    private static final Logger logger = LogManager.getLogger(AfiliadoController.class);

    private final AfiliadoService afiliadoService;

    public AfiliadoController(AfiliadoService afiliadoService) {
        this.afiliadoService = afiliadoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','MODERATOR','ADMIN')")
    @Operation(summary = "Obtener todos los afiliados", description = "Retorna la lista completa de afiliados registrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de afiliados obtenida exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<Afiliado>> getAll() {
        logger.info("Solicitud para obtener todos los afiliados");
        try {
            List<Afiliado> afiliados = afiliadoService.findAll();
            logger.info("Respuesta exitosa: {} afiliados retornados", afiliados.size());
            return ResponseEntity.ok(afiliados);
        } catch (Exception e) {
            logger.error("Error al procesar solicitud de consulta de afiliados", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','MODERATOR','ADMIN')")
    @Operation(summary = "Obtener un afiliado por ID", description = "Retorna un afiliado segun su identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Afiliado encontrado"),
            @ApiResponse(responseCode = "404", description = "Afiliado no encontrado")
    })
    public ResponseEntity<Afiliado> getById(@PathVariable Long id) {
        logger.info("Solicitud para obtener afiliado con id: {}", id);
        try {
            Afiliado afiliado = afiliadoService.findById(id);
            logger.info("Afiliado encontrado con id: {}", id);
            return ResponseEntity.ok(afiliado);
        } catch (Exception e) {
            logger.error("Error al obtener afiliado con id: {}", id, e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @Operation(summary = "Actualizar email y celular de un afiliado", description = "Actualiza unicamente los campos email y celular")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Afiliado actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "404", description = "Afiliado no encontrado")
    })
    public ResponseEntity<Afiliado> update(@PathVariable Long id,
                                           @Valid @RequestBody Afiliado afiliado) {
        logger.info("Solicitud para actualizar afiliado con id: {}", id);
        try {
            Afiliado actualizado = afiliadoService.update(id, afiliado);
            logger.info("Afiliado con id: {} actualizado exitosamente", id);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            logger.error("Error al actualizar afiliado con id: {}", id, e);
            throw e;
        }
    }
}
