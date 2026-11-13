package studying.infrastructure.data;

import java.util.Optional;
import studying.domain.model.Rubric;
import studying.domain.repository.RubricRepository;

/** Хранилище рубрикатора в памяти. */
public final class RubricRepositoryImpl implements RubricRepository {
    private Rubric rubric;
    @Override
    public void save(final Rubric newRubric) {
        this.rubric = newRubric;
    }
    @Override
    public Optional<Rubric> find() {
        return Optional.ofNullable(rubric);
    }
}
