package studying.gateway.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallet_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletTransactionEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletEntity wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_request_id")
    private AiRequestEntity aiRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WalletTransactionType type;

    @Column(nullable = false)
    private long amount;

    @Column(name = "balance_after", nullable = false)
    private long balanceAfter;

    @Column(name = "reserved_after", nullable = false)
    private long reservedAfter;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public WalletTransactionEntity(UUID id, WalletEntity wallet,
                                   AiRequestEntity aiRequest,
                                   WalletTransactionType type, long amount,
                                   Instant createdAt) {
        this.id = id;
        this.wallet = wallet;
        this.aiRequest = aiRequest;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = wallet.getBalance();
        this.reservedAfter = wallet.getReservedBalance();
        this.createdAt = createdAt;
    }

}
