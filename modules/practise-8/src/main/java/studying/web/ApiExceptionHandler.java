package studying.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation
        .ResponseEntityExceptionHandler;
import studying.delivery.application.DeliveryNotFoundException;
import studying.exception.ApplicationException;

/** Переводит ошибки приложения и Spring MVC в единый HTTP-контракт. */
@Slf4j
@RestControllerAdvice
public final class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * Обрабатывает отсутствие ресурса без раскрытия внутренних данных.
     * @param exception ошибка поиска
     * @return HTTP 404
     */
    @ExceptionHandler(DeliveryNotFoundException.class)
    public ResponseEntity<Object> notFound(
            DeliveryNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiError("DELIVERY_NOT_FOUND", "Доставка не найдена"));
    }

    /**
     * Отличает ошибки входных данных от внутренних нарушений состояния.
     * @param exception ошибка приложения
     * @return HTTP 400 или 500
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Object> application(ApplicationException exception) {
        return switch (exception.getCode()) {
            case NOTIFICATION_VALIDATION_ERROR, INVALID_RETRY_POLICY,
                    UNSUPPORTED_NOTIFICATION_CHANNEL ->
                ResponseEntity.badRequest().body(new ApiError(
                        exception.getCode().name(),
                        "Некорректные данные доставки"));
            default -> unexpected(exception);
        };
    }

    /**
     * Сохраняет техническую причину только в серверном журнале.
     * @param exception внутренний сбой
     * @return безопасный HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> unexpected(Exception exception) {
        log.error("Не удалось выполнить запрос", exception);
        return ResponseEntity.internalServerError().body(
                new ApiError("INTERNAL_ERROR", "Внутренняя ошибка сервера"));
    }

    /** Сохраняет статусы и заголовки стандартных ошибок Spring MVC. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("Ошибка обработки HTTP", exception);
        }
        String code = status.value() == HttpStatus.BAD_REQUEST.value()
                ? "INVALID_REQUEST" : "HTTP_" + status.value();
        String message = status.is5xxServerError()
                ? "Внутренняя ошибка сервера" : "Некорректный HTTP-запрос";
        var error = new ApiError(status.is5xxServerError()
                ? "INTERNAL_ERROR" : code, message);
        return super.handleExceptionInternal(exception, error, headers,
                status, request);
    }
}
