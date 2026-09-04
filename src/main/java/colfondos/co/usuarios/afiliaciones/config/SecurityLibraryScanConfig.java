package colfondos.co.usuarios.afiliaciones.config;

import co.mycorp.security.spring_boot_security_jwt.SpringBootSecurityJwtApplication;
import co.mycorp.security.spring_boot_security_jwt.controllers.AuthController;
import co.mycorp.security.spring_boot_security_jwt.controllers.TestController;
import co.mycorp.security.spring_boot_security_jwt.security.WebSecurityConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
        basePackages = "co.mycorp.security.spring_boot_security_jwt",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        WebSecurityConfig.class,
                        SpringBootSecurityJwtApplication.class,
                        AuthController.class,
                        TestController.class
                }
        )
)
public class SecurityLibraryScanConfig {
}