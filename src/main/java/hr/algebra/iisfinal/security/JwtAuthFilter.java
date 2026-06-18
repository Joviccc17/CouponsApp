package hr.algebra.iisfinal.security;

import hr.algebra.iisfinal.model.RefreshToken;
import hr.algebra.iisfinal.service.JwtService;
import hr.algebra.iisfinal.util.CookieUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null) {
            try {
                setAuthentication(jwtService.validateToken(token), request);
            } catch (ExpiredJwtException e) {
                tryRefresh(request, response);
            } catch (Exception ignored) {
            }
        } else {
            tryRefresh(request, response);
        }
        chain.doFilter(request, response);
    }

    private void tryRefresh(HttpServletRequest request, HttpServletResponse response) {

        String refreshTokenStr = cookieUtil.extractRefreshToken(request);

        if (refreshTokenStr == null) return;

        Optional<RefreshToken> opt = jwtService.findRefreshToken(refreshTokenStr);
        if (opt.isEmpty()) return;

        RefreshToken refreshToken = opt.get();
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            jwtService.deleteRefreshToken(refreshTokenStr);
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(refreshToken.getUsername());
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String newAccessToken = jwtService.generateAccessToken(refreshToken.getUsername(), role);
        cookieUtil.setAccessTokenCookie(response, newAccessToken);

        setAuthentication(jwtService.validateToken(newAccessToken), request);
    }

    private void setAuthentication(Claims claims, HttpServletRequest request) {
        String username = claims.getSubject();
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String cookieToken = cookieUtil.extractAccessToken(request);
        if (cookieToken != null) {
            return cookieToken;
        }
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}