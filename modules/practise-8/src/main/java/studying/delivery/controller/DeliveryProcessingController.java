package studying.delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studying.delivery.application.DeliveryApplicationService;
import studying.delivery.dto.ProcessingResponse;

/** Входной HTTP-адаптер синхронной обработки очереди. */
@RestController
@RequestMapping("/api/delivery-processing")
@RequiredArgsConstructor
public final class DeliveryProcessingController {
    private final DeliveryApplicationService application;

    /** @return число сбоев наблюдателей за этот запуск */
    @PostMapping
    public ProcessingResponse processDue() {
        return new ProcessingResponse(application.processDue());
    }
}
