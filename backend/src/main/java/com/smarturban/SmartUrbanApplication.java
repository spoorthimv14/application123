package com.smarturban;

import com.smarturban.entity.Category;
import com.smarturban.entity.Department;
import com.smarturban.repository.CategoryRepository;
import com.smarturban.repository.DepartmentRepository;
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

    @Bean
    public CommandLineRunner initDatabase(CategoryRepository categoryRepository, DepartmentRepository departmentRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                List<Category> defaultCategories = Arrays.asList(
                        new Category("Road/Pothole", "Potholes, damaged roads, asphalt repair", true),
                        new Category("Garbage/Waste", "Uncollected garbage, overflow bins, dumping", true),
                        new Category("Street Light", "Non-functional street lights, dark alleys", true),
                        new Category("Water Supply", "Low pressure, water leakage, pipeline leaks", true),
                        new Category("Drainage", "Blocked drains, sewage overflow, stagnant water", true),
                        new Category("Traffic", "Traffic light failure, illegal parking, signs", true),
                        new Category("Public Toilet", "Dirty public restrooms, maintenance required", true),
                        new Category("Park", "Damaged playground equipment, unmaintained grass", true),
                        new Category("Electricity", "Power outage, hanging wires, transformer issue", true),
                        new Category("Other", "General civic issues and urban infrastructure", true)
                );
                categoryRepository.saveAll(defaultCategories);
            }

            if (departmentRepository.count() == 0) {
                List<Department> defaultDepartments = Arrays.asList(
                        new Department("Public Works Department", "PWD", true),
                        new Department("Sanitation & Waste Management", "SAN", true),
                        new Department("Electrical & Street Lighting", "ELEC", true),
                        new Department("Water Supply & Sewerage", "WATER", true),
                        new Department("Drainage & Flood Control", "DRAIN", true),
                        new Department("Traffic & Urban Transport", "TRAFF", true),
                        new Department("Parks & Horticulture", "PARK", true)
                );
                departmentRepository.saveAll(defaultDepartments);
            }
        };
    }
}
