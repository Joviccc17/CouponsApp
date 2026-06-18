package hr.algebra.iisfinal.security;

import hr.algebra.iisfinal.service.JwtService;
import hr.algebra.iisfinal.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final CookieUtil cookieUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        String accessToken = jwtService.generateAccessToken(username, role);
        String refreshToken = jwtService.generateRefreshToken(username);

        cookieUtil.setAccessTokenCookie(response, accessToken);
        cookieUtil.setRefreshTokenCookie(response, refreshToken);
        response.sendRedirect("/web/dashboard");
    }
}