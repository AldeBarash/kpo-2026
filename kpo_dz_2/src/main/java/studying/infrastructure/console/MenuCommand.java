package studying.infrastructure.console;

import lombok.Getter;

@Getter
enum MenuCommand {
    EXIT("Выход"),
    ADD_STUDENT("Добавить студента"),
    LIST_STUDENTS("Список студентов"),
    ADD_CRITERION("Добавить критерий"),
    LIST_CRITERIA("Список критериев"),
    SET_ESTIMATE("Выставить оценку"),
    CORRECT_ESTIMATE("Исправить оценку"),
    CANCEL_ESTIMATE("Отменить оценку"),
    LIST_ESTIMATES("Показать оценки студента"),
    STUDENT_TOTAL("Итог студента"),
    ALL_TOTALS("Итоги всех студентов"),
    EXPORT("Экспортировать итоги"),
    DEMO("Демо-сценарий");

    private final String label;

    MenuCommand(final String menuLabel) {
        this.label = menuLabel;
    }
}
