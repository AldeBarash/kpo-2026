package studying.telegram.events;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.core.env.Environment;
import org.apache.kafka.common.TopicPartition;
import java.util.Map;

@Configuration
@EnableKafka
@EnableConfigurationProperties({DeliveryProperties.class, studying.telegram.bot.TelegramProperties.class})
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

    @Bean
    public ConsumerFactory<String, String> consumerFactory(Environment env) {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                "bootstrap.servers", env.getRequiredProperty("spring.kafka.bootstrap-servers"),
                "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "enable.auto.commit", false, "auto.offset.reset", "earliest",
                "max.poll.records", 10));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumers, KafkaTemplate<String, String> kafka,
            DeliveryProperties properties, Environment env) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumers);
        factory.setAutoStartup(env.getProperty("app.delivery.consumer-enabled", Boolean.class, true));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        var recoverer = new DeadLetterPublishingRecoverer(kafka,
                (record, exception) -> new TopicPartition(properties.dlt(), record.partition()));
        recoverer.setFailIfSendResultIsError(true);
        recoverer.setWaitForSendResultTimeout(java.time.Duration.ofSeconds(10));
        var errors = new DefaultErrorHandler(recoverer, new FixedBackOff(1000, 2));
        errors.setCommitRecovered(true);
        // A DB outage must not turn otherwise valid billing events into dead letters.
        errors.setBackOffFunction((record, exception) -> {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof org.springframework.dao.DataAccessException
                        || cause instanceof org.springframework.transaction.TransactionException) {
                    return new FixedBackOff(5000, FixedBackOff.UNLIMITED_ATTEMPTS);
                }
            }
            return new FixedBackOff(1000, 2);
        });
        factory.setCommonErrorHandler(errors);
        return factory;
    }
}
