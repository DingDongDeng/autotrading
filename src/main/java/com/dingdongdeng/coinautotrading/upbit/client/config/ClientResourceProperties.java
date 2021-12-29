package com.dingdongdeng.coinautotrading.upbit.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "upbit.client")
@Configuration
public class ClientResourceProperties {

    private String baseUrl;
    private int readTimeout;
    private int connectionTimeout;
}
