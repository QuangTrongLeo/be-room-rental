package tmdt.be_room_rental;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = { OpenAiAutoConfiguration.class })
public class BeRoomRentalApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
        ConfigurableApplicationContext context = SpringApplication.run(BeRoomRentalApplication.class, args);
    }
}