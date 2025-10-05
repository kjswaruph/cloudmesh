package app.cmesh.user;

public record UserInput(
        String firstName,
        String lastName,
        String email,
        String username,
        String password
) {}
