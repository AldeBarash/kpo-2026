package studying.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) { var application = new SpringApplication(GatewayApplication.class);
        application.setDefaultProperties(java.util.Map.of("spring.config.name", "gateway"));
        application.run(args); }
}
