package studying.application.service;

import java.util.List;
import studying.domain.model.Criterion;
import studying.domain.model.CriterionId;
import studying.domain.model.MaxScore;
import studying.domain.model.Rubric;
import studying.domain.model.Weight;
import studying.domain.repository.RubricRepository;

public class RubricService {
    private final RubricRepository rubrics;
    /**
     * Создаёт сервис.
     *
     * @param rubricRepository хранилище рубрикатора
     */
    public RubricService(final RubricRepository rubricRepository) {
        this.rubrics = rubricRepository;
    }
    /**
     * Добавляет критерий в рубрикатор.
     *
     * @param name - название
     * @param maxScore - максимальный балл
     * @param weight - вес
     * @return созданный критерий
     */
    public Criterion addCriterion(final String name,
                                  final int maxScore,
                                  final int weight) {
        final Rubric rubric = Lookups.rubric(rubrics);
        final Criterion criterion = new Criterion(CriterionId.next(), name,
                new MaxScore(maxScore), new Weight(weight));
        rubric.addCriterion(criterion);
        rubrics.save(rubric);
        return criterion;
    }
    /** @return критерии рубрикатора */
    public List<Criterion> listCriteria() {
        return Lookups.rubric(rubrics).criteria();
    }
}
