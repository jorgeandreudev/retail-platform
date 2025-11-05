package com.jorgeandreu.orders.infrastructure.products;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "products-service", configuration = ProductsFeignConfig.class)
public interface ProductsFeignClient {
    @GetMapping("/api/v1/products/{id}")
    ProductDetailsDto getById(@PathVariable("id") String id);
}
