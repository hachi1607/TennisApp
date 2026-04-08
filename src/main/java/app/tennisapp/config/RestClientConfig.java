package app.tennisapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    private final ApiTennisProperties apiTennisProperties;

    @Bean
    public RestClient apiTennisRestClient() {
        return RestClient.builder()
                .baseUrl(apiTennisProperties.getUrl())
                .build();
    }
}