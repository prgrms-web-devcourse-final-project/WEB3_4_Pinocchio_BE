package sns.pinocchio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import sns.pinocchio.config.global.initData.AiUserProperties;

@SpringBootApplication
public class PinocchioApplication {

    public static void main(String[] args) {
        SpringApplication.run(PinocchioApplication.class, args);
    }

}
