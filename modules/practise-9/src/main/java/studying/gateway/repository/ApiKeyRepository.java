package studying.gateway.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studying.gateway.domain.ApiKeyEntity;
import studying.gateway.domain.ApiKeyStatus;

public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, UUID> {

    Optional<ApiKeyEntity> findByKeyHashAndStatus(String keyHash, ApiKeyStatus status);
}
