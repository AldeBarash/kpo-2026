package studying.gateway.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studying.gateway.domain.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByEmailIgnoreCase(String email);
}
