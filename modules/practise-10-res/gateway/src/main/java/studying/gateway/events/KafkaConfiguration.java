package studying.gateway.events;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.core.env.Environment;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(DeliveryProperties.class)
public class KafkaConfiguration {
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(Environment env) {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(Map.of(
                "bootstrap.servers", env.getRequiredProperty("spring.kafka.bootstrap-servers"),
                "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                "value.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                "acks", "all", "enable.idempotence", true,
                "max.block.ms", 5000, "delivery.timeout.ms", 8000, "request.timeout.ms", 5000,
                "linger.ms", 0)));
    }

}
