package app.cmesh.dashboard.repository;

import app.cmesh.dashboard.Project;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, QueryByExampleExecutor<Project> {
}
