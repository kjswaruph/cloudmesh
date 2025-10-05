package app.cmesh.user;

import app.cmesh.util.Role;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class UserController {

    private UserRepository userRepository;
    private UserService userService;

    public UserController(UserRepository userRepository,  UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @QueryMapping
    public List<User> users(@Argument UserInput user) {
        if(user == null){
            return userRepository.findAll();
        }
        User example = new User();
        example.setFirstName(user.firstName());
        example.setLastName(user.lastName());
        example.setEmail(user.email());
        example.setUsername(user.username());
        return userRepository.findAll(Example.of(example));
    }

    @QueryMapping
    public Optional<User> user(@Argument String id) {
        return userRepository.findById(Long.valueOf(id));
    }

    @QueryMapping
    public User me(Authentication authentication ) {
        if(authentication == null){
            return null;
        }

        Object principal = authentication.getPrincipal();
        if(principal instanceof UserDetails userDetails){
            String  username = userDetails.getUsername();
            return userRepository.findUsersByUsername(username);
        } else if (principal instanceof OAuth2User oauthUser) {
            String email = oauthUser.getAttribute("email");
            Optional<User> userOptional = userRepository.findUsersByEmail(email);
            return userOptional.orElseGet(() -> {
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
        log.error("Unknown principal type: {}", principal.getClass().getName());
        return null;
    }

    @MutationMapping
    public User addUser(@Argument String firstName,
                        @Argument String lastName,
                        @Argument String email,
                        @Argument String username,
                        @Argument String password) {
        log.info("Mutation: addUser requested for username: {}", username);
        try {
            return userService.registerUser(firstName, lastName, email, username, password);
        } catch (IllegalArgumentException e) {
            log.error("Error registering user: {}", e.getMessage());
        }
        return null;
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteUser(@Argument String id) {
        log.info("Mutation: deleteUser requested for id: {}", id);
        try{
            userRepository.deleteById(Long.valueOf(id));
            return true;
        }catch (Exception e){
            log.error("Error deleting user with id {}: {}", id, e.getMessage());
            return false;
        }
    }
}
