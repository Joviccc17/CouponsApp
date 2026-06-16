package hr.algebra.iisfinal.config;

import hr.algebra.iisfinal.security.JwtAuthFilter;
import hr.algebra.iisfinal.security.JwtLoginSuccessHandler;
import hr.algebra.iisfinal.security.UserDetailsServiceImpl;
import hr.algebra.iisfinal.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
    private final CookieUtil cookieUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/auth/**", "/soap/**", "/graphql/**", "/h2-console/**"))
            .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/soap/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/login", "/css/**", "/js/**", "/error").permitAll()
                .requestMatchers("/graphiql/**", "/graphql/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/weather").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/coupons/**").hasAnyRole("READ_ONLY", "FULL_ACCESS")
                .requestMatchers(HttpMethod.POST, "/api/coupons/xml").hasRole("FULL_ACCESS")
                .requestMatchers(HttpMethod.POST, "/api/coupons/json").hasRole("FULL_ACCESS")
                .requestMatchers(HttpMethod.POST, "/api/coupons/**").hasRole("FULL_ACCESS")
                .requestMatchers(HttpMethod.PUT, "/api/coupons/**").hasRole("FULL_ACCESS")
                .requestMatchers(HttpMethod.DELETE, "/api/coupons/**").hasRole("FULL_ACCESS")
                .requestMatchers(HttpMethod.GET, "/web/coupons/delete/**").hasRole("FULL_ACCESS")
                .requestMatchers("/web/coupons/edit/**").hasRole("FULL_ACCESS")
                .requestMatchers("/web/coupons/upload").hasRole("FULL_ACCESS")
                .requestMatchers("/web/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(jwtLoginSuccessHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler((request, response, auth) ->
                        cookieUtil.clearTokenCookies(response))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .userDetailsService(userDetailsService)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}