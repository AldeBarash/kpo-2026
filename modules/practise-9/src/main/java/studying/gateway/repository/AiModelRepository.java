package studying.gateway.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import studying.gateway.domain.AiModelEntity;
import studying.gateway.domain.ModelStatus;

public interface AiModelRepository extends JpaRepository<AiModelEntity, UUID> {

    Optional<AiModelEntity> findByNameAndStatus(String name, ModelStatus status);
}
