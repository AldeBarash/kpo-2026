package studying.application.port;

import java.util.List;
import studying.application.dto.StudentResult;

public interface ResultExporter {
    /**
     * Экспортирует итоги.
     *
     * @param results итоги студентов
     */
    void export(List<StudentResult> results);
}
