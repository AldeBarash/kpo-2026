package studying.gateway.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(nullable = false)
    private long balance;

    @Column(name = "reserved_balance", nullable = false)
    private long reservedBalance;

    @Version
    @Column(nullable = false)
    private long version;

    public WalletEntity(UUID id, UserEntity user) {
        this.id = id;
        this.user = user;
    }

    public long availableBalance() {
        return balance - reservedBalance;
    }

    public void topUp(long amount) {
        balance = Math.addExact(balance, amount);
    }

    public void reserve(long amount) {
        if (amount < 0 || availableBalance() < amount) {
            throw new IllegalStateException("Insufficient credits");
        }
        reservedBalance = Math.addExact(reservedBalance, amount);
    }

    public void charge(long reservedAmount, long actualAmount) {
        if (actualAmount < 0 || actualAmount > reservedAmount
                || reservedBalance < reservedAmount || balance < actualAmount) {
            throw new IllegalStateException("Invalid charge");
        }
        reservedBalance -= reservedAmount;
        balance -= actualAmount;
    }

    public void release(long amount) {
        if (amount < 0 || reservedBalance < amount) {
            throw new IllegalStateException("Invalid release");
        }
        reservedBalance -= amount;
    }
}
