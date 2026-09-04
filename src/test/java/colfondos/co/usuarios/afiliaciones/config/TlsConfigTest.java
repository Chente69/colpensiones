package colfondos.co.usuarios.afiliaciones.config;

import org.apache.catalina.connector.Connector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest
@ActiveProfiles("test")
class TlsConfigTest {

    @Autowired
    private ServletWebServerFactory webServerFactory;

    @Test
    void factoryEsTomcatServlet() {
        assertInstanceOf(TomcatServletWebServerFactory.class, webServerFactory);
    }

    @Test
    void factoryTieneConectorHttpRedirectEnPuerto8080() {
        TomcatServletWebServerFactory tomcatFactory = (TomcatServletWebServerFactory) webServerFactory;
        List<Connector> additionalConnectors = tomcatFactory.getAdditionalConnectors();

        assertNotNull(additionalConnectors, "Debe haber conectores adicionales configurados");
        assertEquals(1, additionalConnectors.size(), "Debe haber exactamente un conector adicional");

        Connector httpConnector = additionalConnectors.get(0);
        assertEquals(8080, httpConnector.getPort(), "El puerto HTTP debe ser 8080");
        assertEquals("http", httpConnector.getScheme(), "El scheme debe ser http");
        assertEquals(8443, httpConnector.getRedirectPort(), "El redirect port debe ser 8443");
    }
}