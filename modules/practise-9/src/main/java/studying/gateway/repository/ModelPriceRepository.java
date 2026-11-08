package studying.gateway.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studying.gateway.domain.ModelPriceEntity;

public interface ModelPriceRepository extends JpaRepository<ModelPriceEntity, UUID> {

    @Query("""
        select price from ModelPriceEntity price
        where price.model.id = :modelId
          and price.validFrom <= :now
          and (price.validTo is null or price.validTo > :now)
        order by price.validFrom desc
        limit 1
        """)
    Optional<ModelPriceEntity> findCurrent(@Param("modelId") UUID modelId,
                                           @Param("now") Instant now);
}
