package studying.gateway.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studying.gateway.service.ChatCompletionService;
import studying.gateway.dto.ChatCompletionRequest;
import studying.gateway.dto.ChatCompletionResponse;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatCompletionController {

    private final ChatCompletionService completionService;

    @PostMapping("/completions")
    public ChatCompletionResponse complete(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ChatCompletionRequest request) {
        return completionService.complete(authorization, idempotencyKey, request);
    }
}
