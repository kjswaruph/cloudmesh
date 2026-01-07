package app.cmesh.controller;

import app.cmesh.user.Role;
import app.cmesh.user.User;
import app.cmesh.user.UserInput;
import app.cmesh.user.UserRepository;
import app.cmesh.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> users(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username) {

        if (firstName == null && lastName == null && email == null && username == null) {
            return ResponseEntity.ok(userRepository.findAll());
        }

        User example = new User();
        example.setFirstName(firstName);
        example.setLastName(lastName);
        example.setEmail(email);
        example.setUsername(username);
        return ResponseEntity.ok(userRepository.findAll(Example.of(example)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> user(@PathVariable String id) {
        return userRepository.findById(UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<User> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        Object principal = authentication.getPrincipal();
        User user = null;

        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            user = userRepository.findUsersByUsername(username);
        } else if (principal instanceof OAuth2User oauthUser) {
            String email = oauthUser.getAttribute("email");
            Optional<User> userOptional = userRepository.findUsersByEmail(email);
            user = userOptional.orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(email);
                newUser.setFirstName("Unknown");
                newUser.setLastName("Unknown");
                newUser.setPassword(null);
                newUser.setRole(Role.USER.name());
                return userRepository.save(newUser);
            });
        }

        if (user != null) {
            return ResponseEntity.ok(user);
        }
        log.error("Unknown principal type: {}", principal.getClass().getName());
        return ResponseEntity.status(500).build();
    }

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody UserInput input) {
        log.info("REST: addUser requested for username: {}", input.username());
        try {
            User created = userService.registerUser(
                    input.firstName(),
                    input.lastName(),
                    input.email(),
                    input.username(),
                    input.password());
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            log.error("Error registering user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("REST: deleteUser requested for id: {}", id);
        try {
            userRepository.deleteById(UUID.fromString(id));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting user with id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
