package com.smarturban.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.smarturban.backend")
@EntityScan(basePackages = "com.smarturban.backend.entity")
@EnableJpaRepositories(basePackages = "com.smarturban.backend.repository")
public class SmartUrbanApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartUrbanApplication.class, args);
    }
}
