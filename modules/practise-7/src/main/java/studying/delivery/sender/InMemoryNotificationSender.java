package studying.delivery.sender;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import studying.delivery.model.AttemptResult;
import studying.delivery.validation.DeliveryValidation;
import studying.delivery.validation.SenderValidator;
import studying.notification.model.Notification;
import studying.profiling.TimedExecution;

/** Отправитель с заданной последовательностью результатов для всех каналов. */
public class InMemoryNotificationSender implements NotificationSender {
    private final DeliveryValidation validation;
    private final SenderValidator senderValidator;
    private final Deque<AttemptResult> results;
    private final List<Notification> received = new ArrayList<>();

    /**
     * Принимает результаты отправок в порядке будущих вызовов.
     * @param outcomes заданная последовательность результатов
     * @param valueValidation проверки обязательных данных
     * @param resultValidator проверки состояния отправителя
     */
    public InMemoryNotificationSender(List<AttemptResult> outcomes,
                                      DeliveryValidation valueValidation,
                                      SenderValidator resultValidator) {
        validation = valueValidation;
        senderValidator = resultValidator;
        validation.requiredElements(outcomes,
                "Последовательность результатов обязательна",
                "Последовательность результатов содержит null");
        results = new ArrayDeque<>(List.copyOf(outcomes));
    }

    /**
     * Выполняет отправку с очередным заданным результатом.
     * Переопределения должны сохранять порядок результатов и учёт вызовов.
     * @param notification отправляемое уведомление
     * @return следующий результат сценария
     */
    @Override
    @TimedExecution(operation = "notification.send")
    public AttemptResult send(Notification notification) {
        validation.required(notification,
                "Уведомление обязательно");
        senderValidator.validateRemaining(results);
        received.add(notification);
        return results.removeFirst();
    }

    /** @return число выполненных отправок */
    public int callCount() {
        return received.size();
    }

    /** @return неизменяемый список полученных уведомлений */
    public List<Notification> receivedNotifications() {
        return List.copyOf(received);
    }
}
