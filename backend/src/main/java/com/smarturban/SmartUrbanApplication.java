package com.smarturban;

import com.smarturban.entity.Category;
import com.smarturban.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class SmartUrbanApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartUrbanApplication.class, args);
    }
}
