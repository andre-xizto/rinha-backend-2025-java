package dev.buskopan.rinha_backend_2025;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigBeans {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }
}
