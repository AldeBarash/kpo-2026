package studying.delivery.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studying.delivery.application.CreateDeliveryCommand;
import studying.delivery.application.DeliveryApplicationService;
import studying.delivery.dto.CreateDeliveryRequest;
import studying.delivery.dto.DeliveryResponse;
import studying.delivery.mapper.DeliveryResponseMapper;

/** Входной HTTP-адаптер создания и чтения доставок. */
@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public final class DeliveryController {
    private final DeliveryApplicationService application;
    private final DeliveryResponseMapper mapper;

    /**
     * Преобразует HTTP-запрос в команду приложения.
     * @param request проверенное тело запроса
     * @return созданная доставка и адрес ресурса
     */
    @PostMapping
    public ResponseEntity<DeliveryResponse> create(
            @Valid @RequestBody CreateDeliveryRequest request) {
        var command = new CreateDeliveryCommand(request.channel(),
                request.recipient(), request.text(), request.priority(),
                request.readyAt(), request.retry().maxAttempts(),
                Duration.ofSeconds(request.retry().baseDelaySeconds()));
        var delivery = application.create(command);
        return ResponseEntity.created(URI.create(
                "/api/deliveries/" + delivery.id()))
                .body(mapper.toResponse(delivery));
    }

    /**
     * Возвращает представление доставки.
     * @param id идентификатор из пути запроса
     * @return состояние доставки
     */
    @GetMapping("/{id}")
    public DeliveryResponse find(@PathVariable("id") UUID id) {
        return mapper.toResponse(application.find(id));
    }

    /** @return публичные представления всех доставок */
    @GetMapping
    public List<DeliveryResponse> findAll() {
        return application.findAll().stream().map(mapper::toResponse).toList();
    }
}
