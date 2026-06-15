package hr.algebra.iisfinal.controller;

import hr.algebra.iisfinal.dto.LoginRequest;
import hr.algebra.iisfinal.dto.TokenResponse;
import hr.algebra.iisfinal.model.RefreshToken;
import hr.algebra.iisfinal.security.UserDetailsServiceImpl;
import hr.algebra.iisfinal.service.JwtService;
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

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String accessToken = jwtService.generateAccessToken(request.getUsername(), role);
        String refreshToken = jwtService.generateRefreshToken(request.getUsername());
        return ResponseEntity.ok(TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(900)
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshTokenStr = body.get("refreshToken");
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
        String newRefreshToken = jwtService.generateRefreshToken(refreshToken.getUsername());
        jwtService.deleteRefreshToken(refreshTokenStr);
        return ResponseEntity.ok(TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(900)
                .build());
    }
}