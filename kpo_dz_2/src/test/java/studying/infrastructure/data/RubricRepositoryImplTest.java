package studying.infrastructure.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.domain.model.Rubric;

/** Тесты {@link RubricRepositoryImpl}. */
@DisplayName("Хранилище рубрикатора (RubricRepositoryImpl)")
class RubricRepositoryImplTest {
    @DisplayName("возвращает пусто, пока рубрика не сохранена")
    @Test
    void emptyByDefault() {
        final RubricRepositoryImpl repo = new RubricRepositoryImpl();
        assertTrue(repo.find().isEmpty());
    }
    @DisplayName("возвращает сохранённую рубрику")
    @Test
    void savesAndFindsRubric() {
        final RubricRepositoryImpl repo = new RubricRepositoryImpl();
        final Rubric rubric = new Rubric("ПОС");
        repo.save(rubric);
        assertSame(rubric, repo.find().orElseThrow());
    }
    @DisplayName("заменяет прежнюю рубрику новой")
    @Test
    void replacesRubric() {
        final RubricRepositoryImpl repo = new RubricRepositoryImpl();
        repo.save(new Rubric("Старая"));
        repo.save(new Rubric("Новая"));
        assertEquals("Новая", repo.find().orElseThrow().getName());
    }
}
