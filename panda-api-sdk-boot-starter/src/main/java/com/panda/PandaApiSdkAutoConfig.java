package com.panda;

import com.panda.client.PandaApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("panda.client")
@Data
@ComponentScan
public class PandaApiSdkAutoConfig {
    private String accessKey;

    private String secretKey;


    @Bean
    public PandaApiClient pandaApiClient() {
        return new PandaApiClient(accessKey, secretKey);
    }

}
