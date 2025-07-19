package dev.buskopan.rinha_backend_2025;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class Configs {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
