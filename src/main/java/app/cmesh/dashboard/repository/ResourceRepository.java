package app.cmesh.dashboard.repository;

import app.cmesh.dashboard.Resource;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.graphql.data.GraphQlRepository;

@GraphQlRepository
public interface ResourceRepository extends JpaRepository<Resource, UUID>, QueryByExampleExecutor<Resource> {
}
