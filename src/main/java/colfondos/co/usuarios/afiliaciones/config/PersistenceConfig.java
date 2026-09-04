package colfondos.co.usuarios.afiliaciones.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {
        "colfondos.co.usuarios.afiliaciones.modelos",
        "co.mycorp.security.spring_boot_security_jwt.models"
})
@EnableJpaRepositories(basePackages = {
        "colfondos.co.usuarios.afiliaciones.repositorios",
        "co.mycorp.security.spring_boot_security_jwt.respository"
})
public class PersistenceConfig {
}