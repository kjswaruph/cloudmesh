package app.cmesh.authentication;

import app.cmesh.authentication.records.AuthRequest;
import app.cmesh.authentication.records.SignupRequest;
import app.cmesh.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.cookie.name:jwt_token}")
    private String cookieName;

    @Value("${jwt.cookie.domain:}")
    private String cookieDomain;

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody AuthRequest authRequest,
            HttpServletResponse response) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password()));

            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            final String jwt = jwtUtils.generateToken(userDetails.getUsername());

            // Set JWT in HttpOnly cookie
            jwtUtils.setJwtCookie(response, jwt);
            log.info("User logged in successfully: {}", authRequest.username());
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "username", userDetails.getUsername()));
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for username: {}", authRequest.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        } catch (Exception e) {
            log.error("Login error for username {}: {}", authRequest.username(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest, HttpServletResponse response) {
        try {
            var user = userService.registerUser(
                    signupRequest.firstName(),
                    signupRequest.lastName(),
                    signupRequest.email(),
                    signupRequest.username(),
                    signupRequest.password());

            // Auto-login after signup
            final String jwt = jwtUtils.generateToken(user.getUsername());
            jwtUtils.setJwtCookie(response, jwt);

            log.info("User registered and logged in successfully: {}", user.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Registration successful",
                    "username", user.getUsername()));
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Signup error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed"));
        }
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        Map<String, Object> map = new HashMap<>();
        if (authentication == null) {
            return map;
        }
        Object principal = authentication.getPrincipal();
        map.put("authorities", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        switch (principal) {
            case UserDetails user -> map.put("username", user.getUsername());
            case OAuth2User oauth2User -> {
                map.put("username", oauth2User.getAttribute("name"));
                map.put("email", oauth2User.getAttribute("email"));
            }
            default -> map.put("username", principal.toString());
        }
        return map;
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, Authentication authentication) {
        try {
            String username = authentication != null && authentication.getName() != null
                    ? authentication.getName()
                    : "unknown";

            Cookie cookie = new Cookie(cookieName, null);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            if (cookieDomain != null && !cookieDomain.isBlank()) {
                cookie.setDomain(cookieDomain);
            }
            response.addCookie(cookie);
            log.info("User logged out successfully: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Logged out successfully",
                    "success", true));

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Logout failed"));
        }
    }
}
