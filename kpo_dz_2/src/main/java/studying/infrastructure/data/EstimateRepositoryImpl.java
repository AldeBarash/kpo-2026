package studying.infrastructure.data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import studying.domain.model.CriterionId;
import studying.domain.model.Estimate;
import studying.domain.model.StudentId;
import studying.domain.repository.EstimateRepository;

/** Хранилище оценок в памяти. */
public final class EstimateRepositoryImpl implements EstimateRepository {
    private final Map<StudentId, Map<CriterionId, Estimate>> storage =
            new HashMap<>();
    @Override
    public void save(final Estimate estimate) {
        storage.computeIfAbsent(estimate.studentId(),
                        id -> new LinkedHashMap<>())
                .put(estimate.criterionId(), estimate);
    }
    @Override
    public Optional<Estimate> find(final StudentId studentId,
                                   final CriterionId criterionId) {
        return Optional.ofNullable(
                storage.getOrDefault(studentId, Map.of()).get(criterionId));
    }
    @Override
    public boolean delete(final StudentId studentId,
                          final CriterionId criterionId) {
        final Map<CriterionId, Estimate> estimates = storage.get(studentId);
        return estimates != null && estimates.remove(criterionId) != null;
    }
    @Override
    public List<Estimate> findByStudent(final StudentId studentId) {
        return List.copyOf(
                storage.getOrDefault(studentId, Map.of()).values());
    }
}
