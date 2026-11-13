package studying.application.dto;

import studying.domain.model.Student;
import studying.domain.model.Total;

/**
 * Итог по студенту.
 *
 * @param student студент
 * @param total его итог
 */
public record StudentResult(Student student, Total total) {
}
