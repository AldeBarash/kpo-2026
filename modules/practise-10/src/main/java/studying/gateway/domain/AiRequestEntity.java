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
@Table(name = "ai_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRequestEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "api_key_id", nullable = false)
    private ApiKeyEntity apiKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private AiModelEntity model;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_price_id", nullable = false)
    private ModelPriceEntity modelPrice;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AiRequestStatus status;

    @Column(name = "max_output_tokens", nullable = false)
    private int maxOutputTokens;

    @Column(name = "reserved_credits", nullable = false)
    private long reservedCredits;

    @Column(name = "actual_cost")
    private Long actualCost;

    @Column(name = "response_content", columnDefinition = "TEXT")
    private String responseContent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "error_code")
    private String errorCode;

    public AiRequestEntity(UUID id, UserEntity user, ApiKeyEntity apiKey,
                           AiModelEntity model, ModelPriceEntity modelPrice,
                           String idempotencyKey, String requestHash,
                           int maxOutputTokens, long reservedCredits, Instant createdAt) {
        this.id = id;
        this.user = user;
        this.apiKey = apiKey;
        this.model = model;
        this.modelPrice = modelPrice;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.status = AiRequestStatus.PROCESSING;
        this.maxOutputTokens = maxOutputTokens;
        this.reservedCredits = reservedCredits;
        this.createdAt = createdAt;
    }

    public void complete(long cost, String content, Instant now) {
        status = AiRequestStatus.COMPLETED;
        actualCost = cost;
        responseContent = content;
        completedAt = now;
    }

    public void fail(String code, Instant now) {
        status = AiRequestStatus.FAILED;
        errorCode = code;
        completedAt = now;
    }
}
