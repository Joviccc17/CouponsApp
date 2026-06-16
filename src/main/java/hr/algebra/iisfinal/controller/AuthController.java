package hr.algebra.iisfinal.controller;

import hr.algebra.iisfinal.dto.LoginRequest;
import hr.algebra.iisfinal.model.RefreshToken;
import hr.algebra.iisfinal.security.UserDetailsServiceImpl;
import hr.algebra.iisfinal.service.JwtService;
import hr.algebra.iisfinal.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final CookieUtil cookieUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request,
                                                     HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String accessToken = jwtService.generateAccessToken(request.getUsername(), role);
        String refreshToken = jwtService.generateRefreshToken(request.getUsername());
        cookieUtil.setAccessTokenCookie(response, accessToken);
        cookieUtil.setRefreshTokenCookie(response, refreshToken);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "role", role,
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request,
                                                       HttpServletResponse response,
                                                       @RequestBody(required = false) Map<String, String> body) {
        String refreshTokenStr = cookieUtil.extractRefreshToken(request);
        if (refreshTokenStr == null && body != null) {
            refreshTokenStr = body.get("refreshToken");
        }
        if (refreshTokenStr == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "refreshToken is required");
        }
        RefreshToken refreshToken = jwtService.findRefreshToken(refreshTokenStr)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            jwtService.deleteRefreshToken(refreshTokenStr);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(refreshToken.getUsername());
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String newAccessToken = jwtService.generateAccessToken(refreshToken.getUsername(), role);
        cookieUtil.setAccessTokenCookie(response, newAccessToken);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        cookieUtil.clearTokenCookies(response);
        return ResponseEntity.ok().build();
    }
}