package app.cmesh.authentication;

import app.cmesh.user.User;
import app.cmesh.user.UserRepository;
import app.cmesh.user.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public OAuth2SuccessHandler(UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws
            IOException, ServletException {

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = getAttributeSafely(oAuth2User, "email");
            String name = getAttributeSafely(oAuth2User, "name");
            String provider = extractProvider(authentication);

            if (email == null || email.isBlank()) {
                log.error("Email not provided by OAuth2 provider: {}", provider);
                handleAuthenticationFailure(request, response, "email_missing");
                return;
            }

            User user = findOrCreateUser(email, name, provider);
            String token = jwtUtils.generateToken(user.getUsername());
            jwtUtils.setJwtCookie(response, token);

            log.info("OAuth2 login successful for user: {} via {}", email, provider);

            String targetUrl = frontendUrl + "/oauth";
            log.info("Redirecting to: {}", targetUrl);
            log.info("Frontend URL configured as: {}", frontendUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }catch (Exception e) {
            log.error("Error during OAuth2 authentication: {}", e.getMessage(), e);
            handleAuthenticationFailure(request, response, "authentication_failed");
        }
    }

    private User findOrCreateUser(String email, String name, String provider) {
        Optional<User> userOptional = userRepository.findUsersByEmail(email);

        return userOptional.orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(email);

            // Parse name safely
            String[] nameParts = parseFullName(name);
            newUser.setFirstName(nameParts[0]);
            newUser.setLastName(nameParts[1]);

            newUser.setPassword(null); // OAuth users don't have passwords
            newUser.setRole(Role.USER.name());

            User savedUser = userRepository.save(newUser);
            log.info("Created new OAuth2 user via {}: {}", provider, email);
            return savedUser;
        });
    }

    private String[] parseFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[]{"Unknown", ""};
        }

        String trimmed = fullName.trim();
        String[] parts = trimmed.split("\\s+", 2);

        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";

        return new String[]{firstName, lastName};
    }

    private String getAttributeSafely(OAuth2User oAuth2User, String attributeName) {
        try {
            Object attribute = oAuth2User.getAttribute(attributeName);
            return attribute != null ? attribute.toString() : null;
        } catch (Exception e) {
            log.warn("Could not extract attribute '{}': {}", attributeName, e.getMessage());
            return null;
        }
    }

    private String extractProvider(Authentication authentication) {
        // Try to extract from OAuth2AuthenticationToken
        if (authentication != null && authentication.getClass().getSimpleName().contains("OAuth2")) {
            try {
                return authentication.getClass()
                        .getMethod("getAuthorizedClientRegistrationId")
                        .invoke(authentication)
                        .toString();
            } catch (Exception e) {
                log.debug("Could not extract provider name: {}", e.getMessage());
            }
        }
        return "oauth2";
    }

    private void handleAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, String error) throws IOException {
        String errorUrl = frontendUrl + "/login?error=" + error;
        getRedirectStrategy().sendRedirect( request, response, errorUrl);
    }

}
