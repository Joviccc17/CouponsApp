package hr.algebra.iisfinal.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import io.netty.resolver.DefaultAddressResolverGroup;

@Configuration
public class AppConfig {

    @Value("${app.stripe.api-key:}")
    private String stripeApiKey;

    @Bean("stripeWebClient")
    public WebClient stripeWebClient() {
        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE);
        return WebClient.builder()
                .baseUrl("https://api.stripe.com")
                .defaultHeader("Authorization", "Bearer " + stripeApiKey)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean("dhmzWebClient")
    public WebClient dhmzWebClient() {
        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE);
        return WebClient.builder()
                .baseUrl("https://vrijeme.hr")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public ServletRegistrationBean<JakartaWebServlet> h2Console() {
        ServletRegistrationBean<JakartaWebServlet> register =
                new ServletRegistrationBean<>(new JakartaWebServlet(), "/h2-console/*");
        register.addInitParameter("webAllowOthers", "true");
        return register;
    }
}
