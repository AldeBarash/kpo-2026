package studying.gateway.repository;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import studying.gateway.domain.UsageRecordEntity;

public interface UsageRecordRepository extends JpaRepository<UsageRecordEntity, UUID> {

    Optional<UsageRecordEntity> findByAiRequestId(UUID aiRequestId);
}
