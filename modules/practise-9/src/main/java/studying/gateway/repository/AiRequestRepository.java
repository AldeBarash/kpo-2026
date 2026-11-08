package studying.gateway.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studying.gateway.domain.AiRequestEntity;

public interface AiRequestRepository extends JpaRepository<AiRequestEntity, UUID> {

    Optional<AiRequestEntity> findByUserIdAndIdempotencyKey(UUID userId,
                                                            String idempotencyKey);
}
