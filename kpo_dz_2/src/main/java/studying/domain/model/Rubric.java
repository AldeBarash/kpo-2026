package studying.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/** Рубрика, владеет списком критериев. */
public final class Rubric {
    @Getter
    private final String name;
    private final List<Criterion> criteria = new ArrayList<>();
    /**
     * Создаёт пустую рубрику.
     *
     * @param rubricName - непустое название
     */
    public Rubric(final String rubricName) {
        if (rubricName == null || rubricName.isBlank()) {
            throw new DomainException(DomainErrorCode.INVALID_NAME,
                    "Название рубрики не может быть пустым");
        }
        this.name = rubricName.trim();
    }
    /** @return копия списка критериев*/
    public List<Criterion> criteria() {
        return List.copyOf(criteria);
    }
    /**
     * Добавляет критерий, если такого названия ещё нет.
     *
     * @param criterion - новый критерий
     */
    public void addCriterion(final Criterion criterion) {
        Objects.requireNonNull(criterion, "criterion");
        final boolean duplicate = criteria.stream()
                .anyMatch(c -> c.getName()
                        .equalsIgnoreCase(criterion.getName()));
        if (duplicate) {
            throw new DomainException(DomainErrorCode.DUPLICATE_CRITERION,
                    "Критерий уже есть в рубрике: " + criterion.getName());
        }
        criteria.add(criterion);
    }
    /**
     * Находит критерий по идентификатору.
     *
     * @param id - идентификатор
     * @return критерий
     */
    public Criterion criterion(final CriterionId id) {
        return criteria.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        DomainErrorCode.CRITERION_NOT_IN_RUBRIC,
                        "Критерия нет в рубрике"));
    }
    /**
     * Проверяет, что балл допустим для критерия рубрики.
     *
     * @param id - идентификатор критерия
     * @param score - балл
     */
    public void checkScore(final CriterionId id, final Score score) {
        if (!criterion(id).accepts(score)) {
            throw new DomainException(DomainErrorCode.SCORE_OUT_OF_RANGE,
                    "Балл больше максимального значения критерия");
        }
    }
}
