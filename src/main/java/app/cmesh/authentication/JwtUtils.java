package app.cmesh.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.cookie.name:jwt_token}")
    private String cookieName;

    @Value("${jwt.cookie.domain:}")
    private String cookieDomain;

    @Value("${jwt.cookie.expiration:36000}")
    private int cookieExpiration;

    @Value("${jwt.cookie.secure:false}")
    private boolean cookieSecure;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username){
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public void setJwtCookie(HttpServletResponse response, String token) {
        // Build Set-Cookie header with SameSite attribute
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(cookieName).append("=").append(token)
                .append("; Path=/")
                .append("; Max-Age=").append(cookieExpiration)
                .append("; HttpOnly");

        if (cookieSecure) {
            cookieHeader.append("; Secure");
        }

        // Use Lax for better compatibility with redirects
        cookieHeader.append("; SameSite=Lax");

        // Set the complete cookie header
        response.addHeader("Set-Cookie", cookieHeader.toString());

        log.debug("JWT cookie set: name={}, maxAge={}, secure={}, httpOnly=true, sameSite=Lax",
                cookieName, cookieExpiration, cookieSecure);
    }

    // Extract username from the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Check if the token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validate the token (check if its still valid and matches the user)
    public boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) &&  !isTokenExpired(token));
    }
}
