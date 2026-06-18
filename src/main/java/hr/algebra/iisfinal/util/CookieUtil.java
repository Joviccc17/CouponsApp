package hr.algebra.iisfinal.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie",
                "access_token=" + token + "; HttpOnly; Path=/; SameSite=Strict; Max-Age=900");
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie",
                "refresh_token=" + token + "; HttpOnly; Path=/; SameSite=Strict; Max-Age=604800");
    }

    public void clearTokenCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
                "access_token=; HttpOnly; Path=/; SameSite=Strict; Max-Age=0");
        response.addHeader("Set-Cookie",
                "refresh_token=; HttpOnly; Path=/; SameSite=Strict; Max-Age=0");
    }

    public String extractAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}