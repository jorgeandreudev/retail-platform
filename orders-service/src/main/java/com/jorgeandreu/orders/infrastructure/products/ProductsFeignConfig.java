package com.jorgeandreu.orders.infrastructure.products;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ProductsFeignConfig {

    @Bean
    RequestInterceptor basicAuth(
            @Value("${products.auth.username}") String u,
            @Value("${products.auth.password}") String p
    ){
        String token = Base64.getEncoder().encodeToString((u + ":" + p).getBytes(StandardCharsets.UTF_8));
        return t -> t.header("Authorization", "Basic " + token);
    }
}