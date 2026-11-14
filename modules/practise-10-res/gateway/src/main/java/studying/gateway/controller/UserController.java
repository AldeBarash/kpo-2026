package studying.gateway.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studying.gateway.service.UserAccountService;
import studying.gateway.dto.ApiKeyResponse;
import studying.gateway.dto.CreateUserRequest;
import studying.gateway.dto.TopUpRequest;
import studying.gateway.dto.UserResponse;
import studying.gateway.dto.WalletResponse;
import studying.gateway.dto.WalletTransactionResponse;

@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAccountService accountService;

    @PostMapping
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = accountService.register(request.email());
        return ResponseEntity.created(URI.create("/api/users/" + response.id()))
                .body(response);
    }

    @PostMapping("/{userId}/top-ups")
    public WalletResponse topUp(@PathVariable UUID userId,
                                @Valid @RequestBody TopUpRequest request) {
        return accountService.topUp(userId, request.amount());
    }

    @PostMapping("/{userId}/api-keys")
    public ResponseEntity<ApiKeyResponse> issueApiKey(@PathVariable UUID userId) {
        return ResponseEntity.status(201).body(accountService.issueApiKey(userId));
    }

    @GetMapping("/{userId}/wallet")
    public WalletResponse wallet(@PathVariable UUID userId) {
        return accountService.wallet(userId);
    }

    @GetMapping("/{userId}/wallet/transactions")
    public Page<WalletTransactionResponse> transactions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return accountService.transactions(userId, page, size);
    }
}
