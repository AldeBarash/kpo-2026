package studying.gateway.internal;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import studying.gateway.dto.WalletResponse;
import studying.gateway.repository.UserRepository;
import studying.gateway.domain.UserStatus;
import studying.gateway.service.ApiException;
import studying.gateway.service.UserAccountService;

@RestController
@RequiredArgsConstructor
public class TelegramLinkController {
    private final LinkCodeService links;
    private final UserAccountService accounts;
    private final UserRepository users;

    @PostMapping("/api/telegram/link-codes")
    public LinkCodeService.LinkCode create(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return links.createCode(authorization);
    }

    @PostMapping("/internal/telegram/link-redemptions")
    public LinkCodeService.Redeemed redeem(@RequestBody LinkCodeService.Redemption request) {
        return links.redeem(request);
    }

    @GetMapping("/internal/users/{userId}/wallet")
    public WalletResponse balance(@PathVariable UUID userId) {
        var user = users.findById(userId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Пользователь не найден"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "USER_BLOCKED", "Пользователь заблокирован");
        }
        return accounts.wallet(userId);
    }
}
