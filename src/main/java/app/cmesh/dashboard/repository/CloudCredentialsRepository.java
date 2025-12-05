package app.cmesh.dashboard.repository;

import app.cmesh.dashboard.CloudCredentials;
import app.cmesh.dashboard.CloudCredentials.CredentialStatus;
import app.cmesh.dashboard.enums.CloudProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.graphql.data.GraphQlRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GraphQlRepository
public interface CloudCredentialsRepository extends JpaRepository<CloudCredentials, UUID> {

    List<CloudCredentials> findByUser_UserId(UUID userId);

    List<CloudCredentials> findByUser_UserIdAndProvider(UUID userId, CloudProvider provider);

    List<CloudCredentials> findByUser_UserIdAndStatus(UUID userId, CredentialStatus status);

    Optional<CloudCredentials> findByCredentialIdAndUser_UserId(UUID credentialId, UUID userId);

    boolean existsByCredentialIdAndUser_UserId(UUID credentialId, UUID userId);

    long countByUser_UserId(UUID userId);

    long countByUser_UserIdAndProvider(UUID userId, CloudProvider provider);

    void deleteByCredentialIdAndUser_UserId(UUID credentialId, UUID userId);

    @Query("SELECT c FROM CloudCredentials c WHERE c.status = 'ACTIVE' AND " +
           "(c.lastValidatedAt IS NULL OR c.lastValidatedAt < :beforeTime)")
    List<CloudCredentials> findCredentialsNeedingValidation(@Param("beforeTime") java.time.Instant beforeTime);
}

