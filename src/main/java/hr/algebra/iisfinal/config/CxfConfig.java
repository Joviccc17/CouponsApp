package hr.algebra.iisfinal.config;

import hr.algebra.iisfinal.soap.CouponSoapServiceImpl;
import jakarta.xml.ws.Endpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CxfConfig {

    private final CouponSoapServiceImpl couponSoapServiceImpl;

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }

    @Bean
    public ServletRegistrationBean<CXFServlet> cxfServlet() {
        return new ServletRegistrationBean<>(new CXFServlet(), "/soap/*");
    }

    @Bean
    public Endpoint couponEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), couponSoapServiceImpl);
        endpoint.publish("/coupons");
        return endpoint;
    }
}