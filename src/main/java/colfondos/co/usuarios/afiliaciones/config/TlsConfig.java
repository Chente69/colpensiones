package colfondos.co.usuarios.afiliaciones.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TlsConfig {

    private static final Logger logger = LogManager.getLogger(TlsConfig.class);

    private static final int HTTP_PORT = 8080;
    private static final int HTTPS_PORT = 8443;

    @Bean
    public ServletWebServerFactory servletContainer() {
        logger.info("Configurando Tomcat con HTTPS en puerto {} y redirect desde puerto {}", HTTPS_PORT, HTTP_PORT);
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                // Configuracion de seguridad adicional si se requiere en el futuro
            }
        };
        tomcat.setAdditionalConnectors(List.of(httpConnector()));
        return tomcat;
    }

    private Connector httpConnector() {
        logger.info("Creando connector HTTP en puerto {} con redirect a HTTPS puerto {}", HTTP_PORT, HTTPS_PORT);
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(HTTP_PORT);
        connector.setSecure(false);
        connector.setRedirectPort(HTTPS_PORT);
        return connector;
    }
}