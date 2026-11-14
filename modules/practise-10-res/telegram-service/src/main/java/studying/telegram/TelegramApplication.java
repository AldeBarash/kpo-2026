package studying.telegram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TelegramApplication {
    public static void main(String[] args) { var application = new SpringApplication(TelegramApplication.class);
        application.setDefaultProperties(java.util.Map.of("spring.config.name", "telegram"));
        application.run(args); }
}
