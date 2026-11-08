package studying.gateway.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usage_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageRecordEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_request_id", nullable = false, unique = true)
    private AiRequestEntity aiRequest;

    @Column(name = "prompt_tokens", nullable = false)
    private int promptTokens;

    @Column(name = "completion_tokens", nullable = false)
    private int completionTokens;

    @Column(name = "total_tokens", nullable = false)
    private int totalTokens;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UsageRecordEntity(UUID id, AiRequestEntity aiRequest, int promptTokens,
                             int completionTokens, String modelName, Instant createdAt) {
        this.id = id;
        this.aiRequest = aiRequest;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = Math.addExact(promptTokens, completionTokens);
        this.modelName = modelName;
        this.createdAt = createdAt;
    }

}
