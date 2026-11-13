package studying.domain.repository;

import java.util.Optional;
import studying.domain.model.Rubric;

/** Порт хранилища рубрикатора (он один на приложение). */
public interface RubricRepository {
    /**
     * Сохраняет рубрику.
     *
     * @param rubric - рубрика
     */
    void save(Rubric rubric);
    /** @return рубрика, если уже создана */
    Optional<Rubric> find();
}
