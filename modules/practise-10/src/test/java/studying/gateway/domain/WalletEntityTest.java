package studying.gateway.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WalletEntityTest {

    @Test
    void reservesChargesAndReleasesCredits() {
        WalletEntity wallet = walletWithBalance(1_000);

        wallet.reserve(300);
        assertEquals(700, wallet.availableBalance());

        wallet.charge(300, 120);

        assertEquals(880, wallet.getBalance());
        assertEquals(0, wallet.getReservedBalance());
        assertEquals(880, wallet.availableBalance());
    }

    @Test
    void rejectsReservationAboveAvailableBalance() {
        WalletEntity wallet = walletWithBalance(100);
        wallet.reserve(80);

        assertThrows(IllegalStateException.class, () -> wallet.reserve(21));
    }

    private WalletEntity walletWithBalance(long balance) {
        UserEntity user = new UserEntity(UUID.randomUUID(), "student@example.com",
                UserStatus.ACTIVE, Instant.now());
        WalletEntity wallet = new WalletEntity(UUID.randomUUID(), user);
        wallet.topUp(balance);
        return wallet;
    }
}
