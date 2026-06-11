package tmdt.be_room_rental.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@Data
public class OpenAIConfig {

    @Value("${spring.ai.openai.chat.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.chat.options.model}")
    private String modelName;

    @Value("${spring.ai.openai.chat.completions-path:/v1beta/openai/chat/completions}")
    private String completionsPath;

    @Bean
    public RestClient aiRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
