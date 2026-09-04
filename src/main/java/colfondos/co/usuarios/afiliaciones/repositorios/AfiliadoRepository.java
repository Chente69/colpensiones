package colfondos.co.usuarios.afiliaciones.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import colfondos.co.usuarios.afiliaciones.modelos.Afiliado;

public interface AfiliadoRepository extends JpaRepository<Afiliado, Long> {
}