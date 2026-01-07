package app.cmesh.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, QueryByExampleExecutor<User> {
    Optional<User> findUsersByEmail(String email);

    User findUsersByUsername(String username);
}
