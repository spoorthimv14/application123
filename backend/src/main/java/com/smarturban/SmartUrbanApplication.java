package com.smarturban;

import com.smarturban.entity.Category;
import com.smarturban.entity.Department;
import com.smarturban.entity.Role;
import com.smarturban.entity.User;
import com.smarturban.repository.CategoryRepository;
import com.smarturban.repository.DepartmentRepository;
import com.smarturban.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class SmartUrbanApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartUrbanApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(
            CategoryRepository categoryRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${admin.email:admin@smarturban.com}") String adminEmail,
            @Value("${admin.password:Admin@12345}") String adminPassword,
            @Value("${admin.full-name:System Administrator}") String adminFullName,
            @Value("${admin.phone:+91 99999 99999}") String adminPhone
    ) {
        return args -> {
            // Seed Categories
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

            // Seed Departments
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

            // Seed Admin Account
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = new User(
                        adminFullName,
                        adminEmail,
                        adminPhone,
                        passwordEncoder.encode(adminPassword),
                        Role.ADMIN,
                        "SmartUrban Municipal HQ"
                );
                userRepository.save(admin);
            }
        };
    }
}
